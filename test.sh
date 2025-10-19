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

# --------------------------------------------------------------
# 1. Register and login multiple test users
# --------------------------------------------------------------
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
echo -e "\n"

# --------------------------------------------------------------
# 2. Create sample media entries for testing CRUD operations
# --------------------------------------------------------------
# This simulates different types of media being added by user "jess".
# Each entry is created via POST /media with metadata such as genre and age restriction.
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
  media_id=$(curl -s -X POST $BASE/media \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${USERS[jess]}" \
    -d "$payload" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+')
  MEDIA_IDS[$i]=$media_id
  echo "Created Media ID: $media_id"
  echo ""
  i=$((i+1))
done

# --------------------------------------------------------------
# 3. Create random ratings for all media by random users
# --------------------------------------------------------------
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
  # Random number of users rate this media
  num_users=$(( (RANDOM % ${#USERS[@]}) + 1 ))
  rated_users=( $(shuf -e "${!USERS[@]}" -n $num_users) )

  for username in "${rated_users[@]}"
  do
    stars=$(( (RANDOM % 5) + 1 ))
    comment=${COMMENT_TEMPLATES[$RANDOM % ${#COMMENT_TEMPLATES[@]}]}

    echo -e "\n"
    echo "==> $username rates media $media_id ($stars stars, comment: $comment)"
    curl -s -X POST $BASE/ratings \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${USERS[$username]}" \
      -d "{\"mediaId\":\"$media_id\",\"stars\":$stars,\"comment\":\"$comment\"}"
    echo -e "\n"
  done
done

# --------------------------------------------------------------
# 4. Like random ratings (simulates social interaction)
# --------------------------------------------------------------
for media_id in "${MEDIA_IDS[@]}"
do
  # Fetch all ratings for a given media
  all_ratings=( $(curl -s -X GET "$BASE/ratings?mediaId=$media_id" \
     -H "Authorization: Bearer ${USERS[jess]}" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+') )



  # Randomly pick up to 3 ratings to like
  selected_ratings=( $(shuf -e "${all_ratings[@]}" -n $(( (RANDOM % 3) + 1 ))) )

  for rating_id in "${selected_ratings[@]}"
  do
    liked_users=( $(shuf -e "${!USERS[@]}" -n $(( (RANDOM % 2) + 1 ))) )
    for username in "${liked_users[@]}"
    do
	  echo -e "\n"
      echo "==> $username likes rating $rating_id"
      curl -s -X PUT "$BASE/ratings/$rating_id/like" \
        -H "Authorization: Bearer ${USERS[$username]}"
	  echo ""
    done
  done
done
echo -e "\n"

# --------------------------------------------------------------
# 5. Add media to favorites for each user
# --------------------------------------------------------------
for username in "${!USERS[@]}"
do
  for media_id in "${MEDIA_IDS[@]}"
  do
    echo -e "\n"
    echo "==> $username adds media $media_id to favorites"
    curl -s -X POST "$BASE/favorites?mediaId=$media_id" \
      -H "Authorization: Bearer ${USERS[$username]}"
    echo -e "\n"
  done
done
echo -e "\n"

# --------------------------------------------------------------
# 6. Retrieve leaderboard (most active users)
# --------------------------------------------------------------
echo -e "\n"
echo "==> Leaderboard"
curl -s -X GET $BASE/leaderboard
echo -e "\n"

# ==============================================================
# 7. Retrieve profile data for each logged-in user
# ==============================================================
echo -e "\n"
for username in "${!USERS[@]}"
do
  echo "==> Profile for $username"
  curl -s -X GET "$BASE/profile" \
    -H "Authorization: Bearer ${USERS[$username]}"
  echo -e "\n"
done
echo -e "\n"