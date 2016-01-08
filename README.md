# ZOOMA

ZOOMA is an optimal ontology mapping application developed by the Functional Genomics Production team at the European Bioinformatics Institute.

This is the publicly available code repository for ZOOMA.  Currently, this only includes the core libraries for ZOOMA - there are no generic user interface implementations available, only the EBI internal version that is visible at the homepage listed below.  We're in the process of adding support for building customized versions of ZOOMA with your own data, and we will update these pages when this is available.

Developer documentation is available as part of the ZOOMA documentation at the link given below.  If you can't find what you need there, feel free to email the lead developer, Tony Burdett, on tburdett@ebi.ac.uk.

## Links

### ZOOMA homepage

http://www.ebi.ac.uk/spot/zooma

### ZOOMA documentation

http://www.ebi.ac.uk/spot/zooma/docs

### EBI

http://www.ebi.ac.uk

### Dependencies

ZOOMA depends on the lodestar project to produce it's SPARQL endpoint.  The source for this project is embedded as a submodule, served from the GitHUb repository https://github.com/EBISPOT/lodestar.  See this project for more details. When building from source, the latest version of this codebase will be incorporated into the build.
