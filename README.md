# Playground - A PUBG Tracker

This little hack of an app uses the [pubgtracker.com REST API](https://pubgtracker.com/site-api) to put
together a simple ranking table for a small community.

In order for it to function, two files need to be present in the working directory;

  - `players.txt` - a new-line separated file of PUBG player names.
  - `auth.txt` - a one-line file containing only the [pubgtracker.com REST API](https://pubgtracker.com/site-api) `TRN-Api-Key` header for authentication.
  
This app requires much TLC (ie the code is **very** bad!). 

Splitting the `Main` class out into appropriate components, adding a real view layer and adding some test coverage would be a great start.

A list of nice-to-haves (aka the never-will-get-dones):
 - Addition of a H2 DB to store historical stat data.
 - Creation of player pages.
 - Ranking-over-time graphs.
 - Ability to add custom columns to the main table.
 - Ability to sort columns on main table.