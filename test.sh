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
# ==============================================================

BASE=http://localhost:8080/api

echo "=============================="
echo "=== Media Ratings Platform Test ==="
echo "=============================="

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
  local token=$(curl -s -X POST $BASE/users/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}" | grep -oP '"token"\s*:\s*"\K[^"]+' | tr -d '\n')
  echo $token
}

# ----------------------------
# Register multiple users
# ----------------------------
# This section registers a few demo users (jess, roza, rina, india)
# and logs them in to store their tokens for later authenticated requests.
declare -A USERS
for username in jess roza rina india
do
  echo -e "\n"
  echo "==> Register user $username"
  curl -s -X POST $BASE/users/register \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"secret\"}"
  echo -e "\n"
  USERS[$username]=$(login $username secret)
  echo "Token for $username: ${USERS[$username]}"
done
echo ""

# ----------------------------
# Create media entries (Movies, Series, Games, etc.)
# ----------------------------
# This simulates different types of media being added by user "jess".
# Each entry is created via POST /media with metadata such as genre and age restriction.
declare -A MEDIA_IDS

MEDIA_PAYLOADS=(
  '{"title":"Inception","description":"Dream movie","mediaType":"MOVIE","releaseYear":2010,"genreList":["SCIFI","ACTION"],"ageRestriction":12}'
  '{"title":"The Office","description":"Comedy series","mediaType":"SERIES","releaseYear":2005,"genreList":["COMEDY"],"ageRestriction":6}'
  '{"title":"Resident Evil","description":"Horror Game","mediaType":"GAME","releaseYear":2002,"genreList":["HORROR","ACTION"],"ageRestriction":16}'
  '{"title":"Planet Earth","description":"Documentary about nature","mediaType":"MOVIE","releaseYear":2006,"genreList":["DOCUMENTARY"],"ageRestriction":0}'
)

i=1
for payload in "${MEDIA_PAYLOADS[@]}"
do
  echo "==> User jess creates media $i"
  # Create a media entry and extract its generated ID
  media_id=$(curl -s -X POST $BASE/media \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${USERS[jess]}" \
    -d "$payload" | grep -oP '"id"\s*:\s*\K\d+')
  MEDIA_IDS[$i]=$media_id
  echo "Created Media ID: $media_id"
  echo ""
  i=$((i+1))
done

# ----------------------------
# Users rate each media item (random number of users per media)
# ----------------------------
# Every media is rated by random subset of users
# with a random star rating (1–5), random comment
# and leaves a short comment. Ratings are initially marked as "PENDING".
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
  # Choose a random subset of users to rate this media
  num_users=$(( (RANDOM % ${#USERS[@]}) + 1 ))  # at least 1 user
  rated_users=( $(shuf -e "${!USERS[@]}" -n $num_users) )

  for username in "${rated_users[@]}"
  do
    stars=$(( (RANDOM % 5) + 1 ))
    comment=${COMMENT_TEMPLATES[$RANDOM % ${#COMMENT_TEMPLATES[@]}]}
    status="PENDING"

    echo -e "\n"
    echo "==> $username rates media $media_id ($stars stars, $status, comment: $comment)"
    curl -s -X POST $BASE/ratings \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${USERS[$username]}" \
      -d "{\"mediaId\":$media_id,\"stars\":$stars,\"comment\":\"$comment\"}"
    echo ""
  done
done

# ----------------------------------
# Users like other ratings randomly
# ----------------------------------
# The endpoint PUT /ratings is assumed to toggle or register a like.
for media_id in "${MEDIA_IDS[@]}"
do
  all_ratings=( $(curl -s -X GET "$BASE/ratings/media/$media_id" \
    -H "Authorization: Bearer ${USERS[jess]}" | grep -oP '"id"\s*:\s*\K\d+') )

  # Pick 1–3 random ratings per media
  selected_ratings=( $(shuf -e "${all_ratings[@]}" -n $(( (RANDOM % 3) + 1 ))) )

  for rating_id in "${selected_ratings[@]}"
  do
    # For each selected rating, pick 1–2 random users to like it
    liked_users=( $(shuf -e "${!USERS[@]}" -n $(( (RANDOM % 2) + 1 ))) )
    for username in "${liked_users[@]}"
    do
      echo "==> $username likes rating $rating_id"
      curl -s -X PUT "$BASE/ratings/$rating_id/like" \
        -H "Authorization: Bearer ${USERS[$username]}"
    done
  done
done


# ----------------------------
# Users add favorite media
# ----------------------------
# Each user adds every media item to their favorites list.
for username in "${!USERS[@]}"
do
  for media_id in "${MEDIA_IDS[@]}"
  do
    echo -e "\n"
    echo "==> $username adds media $media_id to favorites"
    curl -s -X POST "$BASE/favorites?mediaId=$media_id" \
      -H "Authorization: Bearer ${USERS[$username]}"
    echo ""
  done
done

# ----------------------------
# Leaderboard request
# ----------------------------
# Retrieves and displays the global leaderboard via GET /leaderboard.
echo -e "\n"
echo "==> Leaderboard"
curl -s -X GET $BASE/leaderboard
echo -e "\n"

# ----------------------------
# Retrieve each user's profile info
# ----------------------------
# GET /users/profile?username=<username> to show individual stats and activity.
for username in "${!USERS[@]}"
do
  echo "==> Profile for $username"
  curl -s -X GET "$BASE/users/profile?username=$username" \
    -H "Authorization: Bearer ${USERS[$username]}"
  echo -e "\n"
done

# ----------------------------
# Confirm first few ratings by their creators
# ----------------------------
# The script simulates the creator (jess) confirming the first few ratings.
# Confirmation might change status from PENDING → CONFIRMED.
echo -e "\n==> Confirm first few ratings by their creators"

# Ratings, die jess erstellt hat, abrufen
user_ratings=( $(curl -s -X GET "$BASE/ratings/user" \
    -H "Authorization: Bearer ${USERS[jess]}" | grep -oP '"id"\s*:\s*\K\d+') )

# Confirm first 3 ratings
for rating_id in "${user_ratings[@]:0:3}"
do
  echo "Confirm rating $rating_id by jess"
  curl -s -X PUT "$BASE/ratings/$rating_id/confirm" \
    -H "Authorization: Bearer ${USERS[jess]}"
done


# ----------------------------
# Retrieve each user's rating history
# ----------------------------
# GET /ratings/user shows all ratings submitted by the authenticated user.
for username in "${!USERS[@]}"
do
  echo -e "\n==> Rating history for $username"

	user_id=$(curl -s -X GET "$BASE/users/profile?username=$username" \
		-H "Authorization: Bearer ${USERS[$username]}" | grep -oP '"id"\s*:\s*\K\d+')

	user_ratings_json=$(curl -s -X GET "$BASE/ratings/user?userId=$user_id" \
		-H "Authorization: Bearer ${USERS[$username]}")


  echo "$user_ratings_json"

done


# ----------------------------
# Get personalized recommendations for each user
# ----------------------------
# GET /recommendations returns media suggestions based on user behavior.
for username in "${!USERS[@]}"
do
  echo -e "\n"
  echo -e "\n==> Recommendations for $username"
  curl -s -X GET "$BASE/recommendations" \
    -H "Authorization: Bearer ${USERS[$username]}"
done
