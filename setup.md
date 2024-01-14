```
jackd -d alsa -d hw:3
```

Open a clj file

`M-x cider-jack-in`, use whatever isn't lein.

Recommend enabling paredit and ryk-mode or maybe set up clojure mode hooks for those i guess.

Run `M-x cider-quit` once you are done, then kill the jackd process.