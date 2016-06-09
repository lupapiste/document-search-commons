# document-search-commons

Common document search related code shared between lupadoku and onkalo applications.

To compile SCSS files to CSS, install the required Ruby dependencies:

    gem install bundler
    bundle install

and watch for SCSS changes and compile main.css (`lein scss :dev auto`
seems not to pick up changes in partials):

    ./sass-watch.sh

Uberjar build will recompile the SCSS sources to a compressed CSS file.

## License

Copyright © 2016 Solita

Distributed under the European Union Public Licence (EUPL) version 1.1.
