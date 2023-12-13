package ru.borshchevskiy.filestorage.web.breadcrumbs;

import lombok.Data;

import java.util.List;

/**
 * Class represents breadcrumbs object which is displayed on web pages.
 */
@Data
public class Breadcrumbs {

    private String path;

    private List<String> crumbsNames;

    private List<String> crumbsLinks;
}
