
## Version update history (print version 0.2)

#### v0.2.3

(2018-10-22）

*	(FTM）Support VRC6 sum MMC5 core single tone;
*	(Mixer）XgmSoundMixer Mixer, also referred to as a sound effect device;

#### v0.2.4

(2018-10-29）

*	(NSF）Completed Nsf partial dyeing method, currently used NsfRender type of working method and FamiTrackerRender's working method is homologous;
*	(NSF）Support 2A03, 2A05, VRC6 sum MMC5 core single tone;

(2018-10-31）

*	(NSF）Support FDS core piece track;
*	(FTM）Support FDS core piece track;
*	(Mixer）Support FDS single-core music broadcasting;

(2018-11-01）

*	(FTM）Support FDS FamiTracker effect;

#### v0.2.5

(2018-11-03）

*	(FTM）Support for FTM generative TXT text, use FtmAudio for text generation. With supporting core piece 2A03, 2A05, VRC6, MMC5 Sum FDS;
*	(FTM）When FtmAudio was created, different news was heard;
*	(BUG fix)(FTM）Repair Gxx Prolonged effect of new product Gxx Effect of production time;
*	(BUG fix)(FTM）Modification of the hierarchy (ARPEGGIO) Reconciliation method (RELATIVE) Time-related problems;

#### v0.2.6

(2018-11-06）

*	(NSF）Support N163 core piece track;
*	(FTM）Support N163 core monophonic, support added to N163's FTM text;
*	(Mixer）Support N163 Single-core music broadcast;
*	(BUG fix)(FTM) Modification 3xx 300 effect change after modification pitch state still exists BUG;

(2018-11-07）

*	(FTM）Support added to N163's TXT text;

#### v0.2.7

(2018-11-11）

*	(NSF）Support VRC7 core track;
*	(Mixer）Support VRC7 Single-core music broadcast;

(2018-11-13）

*	(FTM）Support VRC7 core single tone, support added to VRC7's FTM text;

(2018-11-14）

*	(FTM）Support added to VRC7's TXT text;

#### v0.2.8

(2018-11-17）

*	(NSF）Support S5B core piece track;
*	(Mixer）Support S5B single-core music broadcast;
*	(BUG fix)(NSF）(Mixer）Modifications included Envelope part Noise
*	(BUG fix)(Mixer）Fixed a bug in the Noise track that caused the track sound to be too loud due to a sampling error;
	The problem is that the Xgm mixer appears;
*	(BUG fix)(NSF）Edit NsfRenderer in ready(NsfAudio) method BUG;
*	(BUG FIX) (NSF) Fixed the bug that the DPCM track could not acquire sample data if Bank expansion ROM was used;

(2018-11-18）

*	(NSF）Currently NSF part also supports Blip mixer;
*	(Mixer）Blip mixer support broadcast S5B road;

#### v0.2.9

(2018-11-21）

*	(FTM）Replenishment and change dissemination speed function;

(2018-11-22）

*	(NSF）Replenishment and change dissemination speed function;

(2018-11-23）

*	(FTM）(NSF）Filling up the dyeing device's exit short number of connections;
*	(FTM）(NSF）Improved skip function
*	(FTM）Functions such as the switchTo function, and the position of the next line to be released;
*	(BUG fix)(NSF）Edit 2A03 Core piece path location enable=false when the BUG is still broadcast;

(2018-11-24）

*	(BUG fix)(NSF）Fix the bug that DPCM tracks illegally call the mix method during reset, resulting in track reset error;
*	(NSF）Completed 2A03 Rectangular sweep effect;
*	(FTM）Completed 2A03 Rectangular sweep effect;
*	(BUG fix)(NSF）Modification 2A03 Triangular road is linear when the effect is touched.

#### v0.2.10

(2018-11-25）

*	(Mixer）for XGM mixer progresses and improves performance;
*	(FTM）(NSF）Completion restraint Xgm mixer-like approach;

(2018-11-26）

*	(BUG FIX) (FTM) Fixes a bug where the DPCM track was not responding correctly to the Yxx effect;

(2018-11-27）

*	(Mixer）for XGM Mixer progresses to improve performance (second stage);
*	(BUG fix) (NSF) Fixed the bug where FDS tracks could not be sounded due to the main channel being closed;
