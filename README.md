# FastStar - an optimisation extension

FastStar is an optimisation extension/mod for Galimulator 4.7 
with Starloader 0.0.1 (does not require the Starloader API).

In my tests it appears that the extension doubles the framerate in a 750 star
galaxy and a 128x speed. These improvements are done by removing debug and
profiling code which was otherwise very heavy on the heap (around 85% 
of allocated space is allocated to longs used for this code), it also should
perform a lot less System#currentTimeMillis calls. Combined it lessens the
work for GC as well as the VM itself.

While this currently is the only optimisation, it will certainly not be the last
that the extension adds.

## Building
The project is built like pretty much any other extension out there.
Just drag and drop the (unmodified) desktop galimulator jar in the root folder 
and run `gradlew build` after having built and exported the Starloader prjoect.

## Other modding plattforms
Currently only Starloader is supported, while technically I could provide
the optimisations as a binary patch, I doubt it makes any sense as the
entry-level for applying binary patches is much higher than using gradle.
I will however support any other open source modding API that might follow,
even though I doubt the chance of that happening is rather low, so make
me aware once that happens.
