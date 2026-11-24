#!/bin/bash
# Docker HEALTHCHECK for the backend. The JRE image has no curl or wget, and installing one only for
# this would enlarge the image; bash can open a TCP connection by itself through /dev/tcp.
# Healthy = /api/actuator/health answers "status":"UP" (which includes the database check).
set -eu
port="${SERVER_PORT:-8080}"
exec 3<>"/dev/tcp/127.0.0.1/${port}"
printf 'GET /api/actuator/health HTTP/1.0\r\nHost: localhost\r\n\r\n' >&3
grep -q '"status":"UP"' <&3
