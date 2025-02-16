[![Release](https://jitpack.io/v/umjammer/vavi-sound-nsfplayer.svg)](https://jitpack.io/#umjammer/vavi-sound-nsfplayer)
[![Java CI](https://github.com/umjammer/vavi-sound-nsfplayer/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-sound-nsfplayer/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-sound-nsfplayer/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-sound-nsfplayer/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)

# vavi-sound-nsfplayer

<img alt="duke jumps like mario" src="https://github.com/user-attachments/assets/147cc7a3-b1b4-44fc-be17-de20669283af" width="80" />

## Install

* [maven](https://jitpack.io/#umjammer/vavi-sound-nsfplayer)

## Usage

## References

## TODO

---

# [Original](https://github.com/Gnzdream/NsfPlayer)

NsfPlayer (NSF analysis/distribution device), which is based on Java dissemination and NSF text development process.
Mainly supports converting 8-bit audio files such as .nsf (NSF files), .ftm (FamiTracker editor files) into audio sample streams (PCM arrays).
After that, it is used for other purposes, and the tool is used for machining.

* ftm title support version book: <br>
  FamiTracker 0.4.6

### How to use (Instruction)

#### Installation

* Base environment: JRE 1.8

* Not responsible for any packaging. Your Java process is only on demand. Jar packaging is available immediately. <br>
  A jar will be shipped [here](https://github.com/Gnzdream/NsfPlayer/releases).

#### How to dye NSF formal text

The first demand statement is, this is the purpose of the project,
the PCM formal number set / music flow, but it is not broadcast sound,
it is all in the management of the broadcast sound set.

##### First step: `NsfAudio` practical example, packaging NSF audio text.

Step 1: Create `NsfAudio` instance encapsulates the data of the NSF file.

```java
String path = "test/assets/test/Contra.nsf";
NsfAudioFactory factory = new NsfAudioFactory();
NsfAudio nsf;

try {
  nsf = factory.createFromFile(path);
} catch (IOException e) {
  e.printStackTrace();
  return;
}
```

The path is the NSF textual path. The current process is `test/assets/test/`
NSF (FTM and TXT suffix) writing style, How can it not be used, You can do that too.

--- 

##### Second step: Building ``NsfRenderer`` Example, this is general NSF text number installation, and it is a digital dyeing machine.

Step 2: Create ``NsfRenderer`` instance which convents NSF file data to PCM array.

```Java
NsfRenderer renderer = new NsfRenderer();
renderer.ready(nsf, 0);
```

The top usage method ``ready()`` has multiple access methods, so you can decide on the starting broadcasting position.
This is the first example of the beginning of the dyeing process.

---

##### Third step: Kushizome.

Step 3: Render it using a loop block.

```java
BytesPlayer player = new BytesPlayer();
short[] bs = new short[2400];

while (true) {
  int size = renderer.render(bs, 0, bs.length);
  player.writeSamples(bs, 0, size);
}
```

`(new Thread(...))` In the future, Germany and Germany will continue to dye.

Class `BytesPlayer` Its actual use is to broadcast a single byte / short number of pairs.
This is one of the components of the music that I put in front of me and listen to it.

The phrase is actually there, you can use javax as a base music system,
in fact, it is not necessary to use it, so it is convenient to continue to broadcast the music,
and I can use it to produce music. For use on the top surface,
the length of the production is 2400 units, but the length can be changed depending on the practical use.
How do you determine what your main situation is? This is an example of how to use a byte number set.
This is the goal of completion of the two types of formalism, but it is short and short.
How to select and export byte Number of combinations of formalities,
`NsfRenderer` Under the music and formal formalities of listening to music：

 * 48000 Hz, 16 bit signed | little-endian, mono
 
Due to the NSF, there is no way to confirm whether or not the song has been disseminated
in a conclusive manner.

```java
BytesPlayer player = new BytesPlayer();
short[] array = new short[2400];
int silentLen = 0;
int last = 0;

while (true) {
  int len = renderer.renderOneFrame(array, 0, array.length);
  player.writeSamples(array, 0, len);
  
  if (silentLen == 0) {
    last = array[0];
  }
  for (int i = 1; i < array.length; i++) {
    if (array[i] != last) {
      silentLen = 0;
      continue;
    }
  }
  silentLen += len;
  
  if (silentLen >= 144000) {
    break;
  }
}
```

A top-level example shows the progress of each thread after the dyeing process is completed.
When we do not change the number of decisions we make, we will not be able to make any decisions.
`silentLen` The number of individuals who have not been registered. This `silentLen >= 144000`,
immediately after 3 seconds of changing the number, the judgment will be broadcast.

---

#### How to dye FTM formal sentence

##### First step: Building the first demand ``FtmAudio`` Example, packaging FTM sound file.

Step 1: Create ``FtmAudio`` instance encapsulates the data of the FTM file.

```java
String path = "test/assets/test/JtS Stage 3.ftm";
FtmAudio audio = NsfPlayerApplication.app.open(path);
````

In that, path is the FTM sentence-like path. The current process is `test/assets/test/`
FTM statement used during testing, If it doesn't work, you can also use it.

---

##### Second step: Building ``FamiTrackerRenderer`` Example, this is general FTM sentence number setting and conversion to music stream style dyeing device.

Step 2: Create ``FamiTrackerRenderer`` instance which convents FTM file data to PCM array.

```java
FamiTrackerRenderer renderer = new FamiTrackerRenderer(); renderer.ready(audio, 0);
```

The dyeing process is very important in the production process. You can also present many specific abilities.
Adjust the volume of each track, close the track, set the sound, position the track, etc.

The top usage method `ready()` has multiple access methods, so you can decide on the starting broadcasting position.

---

##### Third step: Render.

Step 3: Render it using a loop block.

```java
BytesPlayer player = new BytesPlayer();
short[] bs = new short[2400];

while (true) {
  int size = renderer.render(bs, 0, bs.length);
  player.writeSamples(bs, 0, size);
  
  if (renderer.isFinished()) {
    break;
  }
}
````

You can see this part, NSF and FTM formal dyeing is one pattern and one pattern.
`(new Thread(...))` In the future, Germany and Germany will continue to dye.

Category `BytesPlayer` Its actual use is to broadcast a single byte / short number of pairs.
This is one of the components of the music that I put in front of me and listen to it.
The phrase is actually there, you can use javax as a base music organization, in fact,
it is not necessary to use it, so it is convenient to continue to broadcast the music,
and I can use it to produce music.

Good luck, you've arrived, and now you can hear the sound of the broadcast. Please be careful,
choose "JtS Stage 3.ftm" Music & Music Usage is FC (NES) "RAF world / Journey to Silius 
3rd edition" background music, The book is an unlimited circulation, and when the shop runs
in more than one direction, the shop is in circulation, and the circulation is in direct circulation.

### Primitive process / Connection (Link)

 * nsfplay (C++) <br>
   This is the first step in the construction of the nsfplay C++ item.
   This is the purpose of this item: <br>
   [bbbradsmith/nsfplay](https://github.com/bbbradsmith/nsfplay)

 * FamiTracker (C++) <br>
   This is the engineering and support general.ftm
   The text has been transformed into a music stream. Item connection: <br>
   [Camano/FamiTracker](https://github.com/Camano/FamiTracker)

### Progress

 * Support level

| core piece  | NSF part                     | FTM part    |
|-------------|------------------------------|-------------|
| 2A03 + 2A07 | Supported                    | Supported   |
| VRC6        | Supported                    | Supported   |
| MMC5        | Supported except PCM channel | Supported   |
| FDS         | Supported                    | Supported   |
| N163        | Supported                    | Supported   |
| VRC7        | Supported                    | Supported   |
| S5B         | Supported                    | Unsupported |

* Version

 Current Version v0.3.1
 
 [Version History 0.3.x](doc/version-0.3.md)
 
 [Version History 0.2.x](doc/version-0.2.md)
