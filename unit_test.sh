echo "Versions: "
chef --version
rubocop --version
foodcritic --version

echo "Updating Berkshelf: "
if [ ! -f Berksfile.lock ]; then berks install; else berks update; fi;

echo "Starting chefstyle (rubocop): "
rubocop --color

echo "Starting foodcritic: "
foodcritic .

echo "Starting ChefSpec: "
chef exec rspec --color
