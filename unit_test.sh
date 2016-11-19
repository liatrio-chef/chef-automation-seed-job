echo "Versions: "
chef --version
rubocop --version
foodcritic --version

echo "Starting chefstyle (rubocop): "
rubocop -r chefstyle --color

echo "Starting foodcritic: "
foodcritic .

echo "Starting ChefSpec: "
chef exec rspec --color
