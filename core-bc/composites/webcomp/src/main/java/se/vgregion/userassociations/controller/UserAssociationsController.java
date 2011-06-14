package se.vgregion.userassociations.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 * User: pabe
 * Date: 2011-06-14
 * Time: 15:58
 */

@Controller
@RequestMapping(value = "VIEW")
public class UserAssociationsController {

    @RenderMapping
    public String showPage() {

        return "index";
    }
}
