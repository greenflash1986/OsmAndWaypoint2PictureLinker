# OsmAnd Waypoint2Picture Linker

This is a little tool to connect a gpx waypoint file with pictures and convert the picture filenames to the internal format of Audio/ Video notes for OsmAnd. Additionally the original pictures will be geotagged with [exiftool](https://www.sno.phy.queensu.ca/~phil/exiftool/).

It's NOT an outofthebox solution and you need to be able to use eclipse and gradle to build and run it. There is no "Release" planned.

## How to use

You need a gpx waypoint file and a folder with corresponding pictures.The picture files has to have the same name like the waypoint before the "_" or ".jpg".

Example:
    Waypointname: "Hello World", The following picture names are possible: Hello World.jpg, Hello World\_sldf.jpg, Hello World\_1.jpg and so on

After start of the tool you will be asked 3 times to select a folder / file.
The 1st question is for the waypoint file. 2nd one is for the folder where the tool should look for the corresponding pictures. The 3rd question is the folder for the output for osmand structure. 

The tool will copy the pictures to the output folder and rename them to the internal format of osmand. Also the gpx-file will be copied there and links to the pictures are added.

After this the ORIGINAL pictures are geotagged with exiftool and left in the same folder. Exiftool will create a backup of the pictures in the folder.

## Contributions / Questions

Feel free to get in touch with me about these topics. I will do my best to honor these efforts.

## Disclaimer / Notes

I made this project in my spare time and used this project as playground to improve some of my skills. Because of this I won't promise to do further work on this project. I provided it online on Github for you that you can use the (partially) fixed version. Hopefully it's useful for some people and / or feel free to fork or made contributions. I will do my best to honor these efforts.