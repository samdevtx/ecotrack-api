#!/usr/bin/env python3
"""Wait for a public Spring Actuator endpoint to return HTTP 200 and status UP."""

import argparse
import json
import time
import urllib.error
import urllib.parse
import urllib.request


def wait_for_health(url, attempts=30, interval=20, request_timeout=10):
    parsed = urllib.parse.urlparse(url)
    if parsed.scheme != "https" or not parsed.hostname or parsed.username or parsed.password:
        raise ValueError("Health URL must be an HTTPS URL without credentials")

    print(f"Polling {url}", flush=True)
    for attempt in range(1, attempts + 1):
        try:
            request = urllib.request.Request(url, headers={"Accept": "application/json"})
            with urllib.request.urlopen(request, timeout=request_timeout) as response:
                body = response.read(65_537)
                healthy = (response.status == 200 and len(body) <= 65_536
                           and json.loads(body).get("status") == "UP")
                if healthy:
                    print(f"Healthy: HTTP 200, status UP (attempt {attempt})", flush=True)
                    return True
                print(f"Attempt {attempt}/{attempts}: HTTP {response.status}, status is not UP", flush=True)
        except urllib.error.HTTPError as error:
            print(f"Attempt {attempt}/{attempts}: HTTP {error.code}", flush=True)
            error.close()
        except (urllib.error.URLError, TimeoutError, OSError) as error:
            print(f"Attempt {attempt}/{attempts}: connection error: {error}", flush=True)
        except (ValueError, AttributeError, UnicodeError):
            print(f"Attempt {attempt}/{attempts}: response is not an Actuator health object", flush=True)
        if attempt < attempts:
            time.sleep(interval)

    print(f"Health check failed after {attempts} attempts", flush=True)
    return False


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("url")
    args = parser.parse_args()
    try:
        return 0 if wait_for_health(args.url) else 1
    except ValueError as error:
        parser.error(str(error))


if __name__ == "__main__":
    raise SystemExit(main())
