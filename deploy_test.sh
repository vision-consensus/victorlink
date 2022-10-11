branch=$1
if [ ! -n "$branch" ]
then
  branch=development
  echo "not set branch default branch : master"
else
  echo "build branch "$branch
fi
git checkout $branch
if [ $? -ne 0 ]; then
    echo "git checkout $branch failed"
    exit 9
else
    echo "git checkout $branch succeed"
fi
git pull origin $branch
docker-compose -f docker-compose.yml up -d --build