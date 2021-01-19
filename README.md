# McScan (beta)

This project is archived, please check new version : https://github.com/fhebuterne/McScanKt

McScan analyse a minecraft map and get all items with custom name and / or lore 
from chest, hopper and inventory players (if map contain playerdata).  
McScan does a ranking with items locations and players UUID.

## Requirements

- Java 8  
- 3GB RAM available

## Build

McScan use Gradle 5, to build use this command :

With gradle wrapper (recommended) :  

    gradlew build

With gradle binary :  

    gradle build

## Usage

McScan jar is in `./build/libs/` folder, to use :

    java -jar mc-scan-version.jar --world ./path/to/world
    
## License

[MIT](LICENSE)
