# Setup Clojars Account

Register for an account on clojars.org if needed.

https://clojars.org/register

# Setup GPG

This will be dense but has all the info you need if you get stuck:

```
lein help gpg
```

#### install gpg
Use your package manager.

#### setup a key in gpg
```
gpg --gen-key
```

#### publish gpg key
```
gpg --list-keys
gpg --send-keys <key>
```

#### add the key to your .lein/profiles.clj
``` clojure
    {:user
      {:signing "<key>"}}
```

#### set GPG_TTY so that you can enter a passphrase
    export GPG_TTY=$(tty)

# Get access to smidje group

Get in touch with someone who has access and ask them to add you

https://clojars.org/groups/smidje

# Publish

Make sure that there is an open pull request bumping the version number in both the
smidje.version file and README.md files.

```
lein publish clojars
```
