# document-search-commons

Common document search related code shared between lupadoku and onkalo applications.

Uberjar build will recompile the SCSS sources to a compressed CSS file, but it can also be done like this:

    lein sass4clj once

For development time usage, compile SCSS files to CSS uncompressed:

    lein sass4clj once :output-style :expanded


## License

Copyright © 2021 Cloudpermit Oy

Distributed under the European Union Public Licence (EUPL) version 1.1.
