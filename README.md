## Set Auth Cookie Name

1. Set JwtTokenUtil.TOKEN_NAME
2. Set cookieName in config.js

## Set App Name

1. Set name, title, description in config.js
2. Set name in package.json
3. Set artifactId in pom.xml
4. Set finalName in pom.xml
5. Generate Figlet and update on banner.txt https://www.askapache.com/online-tools/figlet-ascii/

## Set DB Credentials

1. Enter the credetials at database.properties

## Production Setup

1. Uncomment maven-resources-plugin and com.github.eirslett plugins in pom.xml
2. Set PROXY = '' in proxy.js

## Development Setup

1. Comment maven-resources-plugin and com.github.eirslett plugins in pom.xml
2. Set PROXY = '<server-url>' in proxy.js

## Add a new menu item

1. Add a new entry in menuItems


Any update to this Repo will be updated to Dev Team Channel
