FIT Layout Analysis Framework - BCS Segmentation Algorithm
===============================================================
(c) 2018-2019 Tomáš Lengál, Jan Zelený, Radek Burget (burgetr@fit.vutbr.cz)

This is an experimental re-write of the *Box clustering segmentation* algorithm published in

```
ZELENÝ Jan, BURGET Radek and ZENDULKA Jaroslav. Box Clustering Segmentation: A New Method for Vision-based Page Preprocessing.
In Information Processing and Management. 2017, vol. 53, no. 3, pp. 735-750. ISSN 0306-4573.
```

This project contains a port of the original CSSBox-based implementation to the FitLayout framework.
Since the underlying page representation used by FitLayout is different than the CSSBox model, the
new implementation may give different results in certain situations.

This is a work in progress, several bugs may have been itroduced during the rewrite. Please report any issues
found in the code via GitHub Issues.

When using the project for your scientific work, please cite the related publication:

```
@article{bcs,
    title = "Box clustering segmentation: A new method for vision-based web page preprocessing",
    journal = "Information Processing & Management",
    volume = "53",
    number = "3",
    pages = "735 - 750",
    year = "2017",
    issn = "0306-4573",
    doi = "https://doi.org/10.1016/j.ipm.2017.02.002",
    url = "http://www.sciencedirect.com/science/article/pii/S0306457316301169",
    author = "Jan Zeleny and Radek Burget and Jaroslav Zendulka"
}
```

See the [Builds/ToolsBCS](https://github.com/FitLayout/Builds/tree/master/ToolsBCS) project for an example
of a FitLayout build that includes the BCS support.
