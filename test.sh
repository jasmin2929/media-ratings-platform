#!/bin/bash

# ==============================================================
#  Media Ratings Platform – Automated API Test Script
# ==============================================================

BASE=http://localhost:8080/api

echo "=============================="
echo "=== Media Ratings Platform Test ==="
echo "=============================="

function login() {
  local username=$1
  local password=$2
  local token=$(curl -s -X POST $BASE/users/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}" | grep -oP '"token"\s*:\s*"\K[^"]+' | tr -d '\n')
  echo $token
}

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
  media_id=$(curl -s -X POST $BASE/media \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${USERS[jess]}" \
    -d "$payload" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+')
  MEDIA_IDS[$i]=$media_id
  echo "Created Media ID: $media_id"
  echo ""
  i=$((i+1))
done

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
  num_users=$(( (RANDOM % ${#USERS[@]}) + 1 ))
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
      -d "{\"mediaId\":\"$media_id\",\"stars\":$stars,\"comment\":\"$comment\"}"
    echo ""
  done
done

for media_id in "${MEDIA_IDS[@]}"
do
  all_ratings=( $(curl -s -X GET "$BASE/ratings/media/$media_id" \
    -H "Authorization: Bearer ${USERS[jess]}" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+') )

  selected_ratings=( $(shuf -e "${all_ratings[@]}" -n $(( (RANDOM % 3) + 1 ))) )

  for rating_id in "${selected_ratings[@]}"
  do
    liked_users=( $(shuf -e "${!USERS[@]}" -n $(( (RANDOM % 2) + 1 ))) )
    for username in "${liked_users[@]}"
    do
      echo "==> $username likes rating $rating_id"
      curl -s -X PUT "$BASE/ratings/$rating_id/like" \
        -H "Authorization: Bearer ${USERS[$username]}"
    done
  done
done

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

echo -e "\n"
echo "==> Leaderboard"
curl -s -X GET $BASE/leaderboard
echo -e "\n"

for username in "${!USERS[@]}"
do
  echo "==> Profile for $username"
  curl -s -X GET "$BASE/users/profile?username=$username" \
    -H "Authorization: Bearer ${USERS[$username]}"
  echo -e "\n"
done

echo -e "\n==> Confirm first few ratings by their creators"

user_ratings=( $(curl -s -X GET "$BASE/ratings/user" \
    -H "Authorization: Bearer ${USERS[jess]}" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+') )

for rating_id in "${user_ratings[@]:0:3}"
do
  echo "Confirm rating $rating_id by jess"
  curl -s -X PUT "$BASE/ratings/$rating_id/confirm" \
    -H "Authorization: Bearer ${USERS[jess]}"
done

for username in "${!USERS[@]}"
do
  echo -e "\n==> Rating history for $username"

	user_id=$(curl -s -X GET "$BASE/users/profile?username=$username" \
		-H "Authorization: Bearer ${USERS[$username]}" | grep -oP '"id"\s*:\s*"\K[0-9a-fA-F-]+')

	user_ratings_json=$(curl -s -X GET "$BASE/ratings/user?userId=$user_id" \
		-H "Authorization: Bearer ${USERS[$username]}")

  echo "$user_ratings_json"
done

for username in "${!USERS[@]}"
do
  echo -e "\n"
  echo -e "\n==> Recommendations for $username"
  curl -s -X GET "$BASE/recommendations" \
    -H "Authorization: Bearer ${USERS[$username]}"
done
