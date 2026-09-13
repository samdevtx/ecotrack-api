import contextlib
import importlib.util
import io
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import MagicMock, patch

spec = importlib.util.spec_from_file_location("health_probe", Path(__file__).with_name("wait_for_health.py"))
probe = importlib.util.module_from_spec(spec)
spec.loader.exec_module(probe)
URL = "https://example.com/actuator/health"


def response(body):
    result = MagicMock()
    result.__enter__.return_value = result
    result.status = 200
    result.read.return_value = body
    return result


class HealthProbeTest(unittest.TestCase):
    def test_retries_unavailable_service_then_accepts_up(self):
        unavailable = urllib.error.HTTPError(URL, 503, "Unavailable", {}, None)
        with patch.object(probe.urllib.request, "urlopen", side_effect=[unavailable, response(b'{"status":"UP"}')]) as fetch:
            with patch.object(probe.time, "sleep") as sleep, contextlib.redirect_stdout(io.StringIO()):
                self.assertTrue(probe.wait_for_health(URL, attempts=3))
        self.assertEqual(2, fetch.call_count)
        sleep.assert_called_once_with(20)
        self.assertEqual(10, fetch.call_args.kwargs["timeout"])

    def test_http_200_requires_an_actuator_object_with_status_up(self):
        for body in (b"<html>Welcome</html>", b'{"status":"DOWN"}', b'[{"status":"UP"}]', b"x" * 65_537):
            with self.subTest(body=body[:30]):
                with patch.object(probe.urllib.request, "urlopen", return_value=response(body)):
                    with patch.object(probe.time, "sleep") as sleep, contextlib.redirect_stdout(io.StringIO()):
                        self.assertFalse(probe.wait_for_health(URL, attempts=1))
                    sleep.assert_not_called()

    def test_connection_failures_report_the_cause_without_duplicate_http_codes(self):
        output = io.StringIO()
        with patch.object(probe.urllib.request, "urlopen", side_effect=urllib.error.URLError("DNS lookup failed")):
            with patch.object(probe.time, "sleep") as sleep, contextlib.redirect_stdout(output):
                self.assertFalse(probe.wait_for_health(URL, attempts=2))
        self.assertIn("connection error", output.getvalue())
        self.assertIn("DNS lookup failed", output.getvalue())
        self.assertNotIn("000000", output.getvalue())
        sleep.assert_called_once()

    def test_rejects_empty_insecure_or_credential_bearing_urls_before_requesting(self):
        with patch.object(probe.urllib.request, "urlopen") as fetch:
            for url in ("", "http://example.com/health", "https://user:password@example.com/health"):
                with self.assertRaises(ValueError):
                    probe.wait_for_health(url)
            fetch.assert_not_called()


if __name__ == "__main__":
    unittest.main()
