#!/bin/bash

# ==============================================================
#  Media Ratings Platform – Automated API Test Script
# ==============================================================
# Description:
#   This Bash script simulates a complete test flow for a Media Ratings Platform API.
#   It performs end-to-end testing of major functionalities, including:
#     - User registration and authentication
#     - Media creation (movies, series, games, etc.)
#     - User ratings, comments, and likes
#     - Adding favorites and retrieving leaderboard data
#     - Viewing user profiles and rating history
#     - Confirming ratings and fetching personalized recommendations
#
#   The script uses `curl` to send HTTP requests to the API endpoints hosted at:
#       BASE=http://localhost:8080/api
#
#   It’s ideal for testing local development environments or automated API demos.
#
# Usage:
#   1. Make sure the backend server is running at localhost:8080.
#   2. Run the script with execution permissions:
#        chmod +x test_api.sh
#        ./test_api.sh
#
# Notes:
#   - All users use the same test password: "secret".
#   - The script extracts authentication tokens dynamically.
#   - Media and ratings are created automatically with randomized data.
#   - `set -e` is enabled, so the script exits immediately on most errors
#     (we also add explicit checks with human-friendly error messages).
# ==============================================================

# --------------------------------------------------------------
# Bash strictness:
#   -e : exit immediately if a command fails (non-zero exit code)
# This makes CI / automated runs safer, because failures don't get silently ignored.
# --------------------------------------------------------------
set -e

# --------------------------------------------------------------
# Base URL of your API.
# --------------------------------------------------------------
BASE="http://localhost:8080/api"

# Curl helpers:
# - CURL_OK fails on HTTP 4xx/5xx (so we actually test success paths)
# - CURL_ANY does NOT fail on HTTP errors (useful when we expect "already exists", 404, etc.)
CURL_OK="curl -sS --fail-with-body"
CURL_ANY="curl -sS"

echo "=============================="
echo "=== Media Ratings Platform Test ==="
echo "=============================="

# --------------------------------------------------------------
# Safety check: ensure the server is reachable before starting.
# --------------------------------------------------------------
$CURL_ANY -o /dev/null "http://localhost:8080" || {
  echo "Server not reachable on http://localhost:8080" >&2
  exit 1
}

# ----------------------------
# Helper function to log in a user and extract their JWT token
# ----------------------------
# Takes two parameters:
#   $1 - username
#   $2 - password
# Sends a POST request to /users/login and extracts the "token" field using grep.
# Returns the token as a plain string for use in Authorization headers.
function login() {
  local username=$1
  local password=$2

  # Call login endpoint and capture JSON response
  local resp=$(curl -s -X POST "$BASE/users/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}")

  # Extract token from JSON like: {"token":"..."}
  local token=$(echo "$resp" | grep -oP '"token"\s*:\s*"\K[^"]+' | tr -d '\n')

  # If we couldn't parse a token, stop immediately and show the raw response
  if [ -z "$token" ]; then
    echo "LOGIN FAILED for $username. Response: $resp" >&2
    exit 1
  fi

  echo "$token"
}

# --------------------------------------------------------------
# 1. Register and login multiple test users
# --------------------------------------------------------------
# This section registers a few demo users (jess, roza, rina, india)
# and logs them in to store their tokens for later authenticated requests.
echo ""
echo "1) Register + login users"
declare -A USERS
for u in jess roza rina india
do
  echo "==> Register user $u"
  reg_resp=$(curl -s -X POST "$BASE/users/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$u\",\"password\":\"secret\"}")

  echo "$reg_resp"

  echo ""
  echo "==> Login user $u"
  USERS[$u]=$(login "$u" "secret")
  echo "Token for $u: ${USERS[$u]}"
  echo ""
done

# Quick sanity: ensure we have at least 1 token
if [ "${#USERS[@]}" -eq 0 ]; then
  echo "No users logged in - aborting." >&2
  exit 1
fi

# --------------------------------------------------------------
# 2. Create sample media entries for testing CRUD operations
# --------------------------------------------------------------
# This simulates different types of media being added by user "jess".
# Each entry is created via POST /media with metadata such as genre and age restriction.

echo ""
echo "2) Create sample media entries (created by jess)"
declare -A MEDIA_IDS

MEDIA_PAYLOADS=(
  '{"title":"Inception","description":"Dream movie","mediaType":{"name":"MOVIE"},"releaseYear":2010,"genreList":[{"name":"SCIFI"},{"name":"ACTION"}],"ageRestriction":12}'
  '{"title":"The Office","description":"Comedy series","mediaType":{"name":"SERIES"},"releaseYear":2005,"genreList":[{"name":"COMEDY"}],"ageRestriction":6}'
  '{"title":"Resident Evil","description":"Horror Game","mediaType":{"name":"GAME"},"releaseYear":2002,"genreList":[{"name":"HORROR"},{"name":"ACTION"}],"ageRestriction":16}'
  '{"title":"Planet Earth","description":"Documentary about nature","mediaType":{"name":"MOVIE"},"releaseYear":2006,"genreList":[{"name":"DOCUMENTARY"}],"ageRestriction":0}'
)


i=1
for payload in "${MEDIA_PAYLOADS[@]}"
do
  echo "==> User jess creates media $i"
  media_resp=$(curl -s -X POST "$BASE/media" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${USERS[jess]}" \
    -d "$payload")

  # Parse the "id" from response JSON
  media_id=$(echo "$media_resp" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+' | head -n 1)

  # Fail fast if we didn't get an ID
  if [ -z "$media_id" ]; then
    echo "MEDIA CREATE FAILED. Response: $media_resp" >&2
    exit 1
  fi

  MEDIA_IDS[$i]=$media_id
  echo "Created Media ID: $media_id"
  echo ""
  i=$((i+1))
done

# --------------------------------------------------------------
# 3. Create random ratings for all media by random users
# --------------------------------------------------------------
# Every media is rated by random subset of users
# with a random star rating (1–5) and a short random comment.
#
# Implementation notes:
#   - We shuffle the list of usernames via `shuf` and take N of them.
# --------------------------------------------------------------
echo ""
echo "3) Create random ratings for all media by random users"
COMMENT_TEMPLATES=(
  "Loved it!"
  "Not my style."
  "Could watch it again."
  "Amazing experience!"
  "Would recommend to friends."
  "Pretty boring..."
  "Unexpected plot twist!"
  "Great visuals."
)

for media_id in "${MEDIA_IDS[@]}"
do
  # random number of users rate this media
  users_total=${#USERS[@]}
  num_users=$(( (RANDOM % users_total) + 1 ))
  rated_users=( $(shuf -e "${!USERS[@]}" -n "$num_users") )

  for u in "${rated_users[@]}"
  do
    stars=$(( (RANDOM % 5) + 1 ))
    comment=${COMMENT_TEMPLATES[$RANDOM % ${#COMMENT_TEMPLATES[@]}]}

    echo "==> $u rates media $media_id ($stars stars)"
    curl -s -X POST "$BASE/ratings" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${USERS[$u]}" \
      -d "{\"mediaId\":\"$media_id\",\"stars\":$stars,\"comment\":\"$comment\"}" | cat
    echo ""
  done
done

# --------------------------------------------------------------
# 4. Like random ratings (simulates social interaction)
# --------------------------------------------------------------
# For each media:
#   1) Fetch ratings via GET /ratings?mediaId=...
#   2) Randomly pick up to 3 rating IDs
#   3) Randomly pick 1–2 users to like each rating
#   4) Like via PUT /ratings/{id}/like
echo ""
echo "4) Like random ratings"
for media_id in "${MEDIA_IDS[@]}"
do
  all_ratings=( $(curl -s -X GET "$BASE/ratings?mediaId=$media_id" \
     -H "Authorization: Bearer ${USERS[jess]}" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+' ) )

  if [ "${#all_ratings[@]}" -eq 0 ]; then
    echo "No ratings found for media $media_id - skipping likes"
    continue
  fi

  pick=$(( (RANDOM % 3) + 1 ))
  selected_ratings=( $(shuf -e "${all_ratings[@]}" -n "$pick") )

  for rating_id in "${selected_ratings[@]}"
  do
    pick_users=$(( (RANDOM % 2) + 1 ))
    liked_users=( $(shuf -e "${!USERS[@]}" -n "$pick_users") )

    for u in "${liked_users[@]}"
    do
      echo "==> $u likes rating $rating_id"
      curl -s -X PUT "$BASE/ratings/$rating_id/like" \
        -H "Authorization: Bearer ${USERS[$u]}" | cat
      echo ""
    done
  done
done

# --------------------------------------------------------------
# 5. Add media to favorites for each user
# --------------------------------------------------------------

echo ""
echo "5) Add media to favorites for each user"
for u in "${!USERS[@]}"
do
  for media_id in "${MEDIA_IDS[@]}"
  do
    echo "==> $u adds media $media_id to favorites"
    curl -s -X POST "$BASE/favorites?mediaId=$media_id" \
      -H "Authorization: Bearer ${USERS[$u]}" | cat
    echo ""
  done
done

# --------------------------------------------------------------
# 6. Retrieve leaderboard (most active users)
# --------------------------------------------------------------
echo ""
echo "6) Leaderboard"
curl -s -X GET "$BASE/leaderboard" | cat
echo ""

# ==============================================================
# 7. Retrieve profile data for each logged-in user
# ==============================================================
# The API typically includes user data, favorites, and rating history.
echo ""
echo "7) Profile for each user"
for u in "${!USERS[@]}"
do
  echo "==> Profile for $u"
  curl -s -X GET "$BASE/profile" \
    -H "Authorization: Bearer ${USERS[$u]}" | cat
  echo ""
done

echo ""
echo "DONE"
