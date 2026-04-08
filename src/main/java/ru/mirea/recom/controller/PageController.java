package ru.mirea.recom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/movies")
    public String showMoviesPage(Model model) {
        model.addAttribute("pageTitle", "Подбор фильмов");
        return "movies";
    }

    @GetMapping("/games")
    public String showGamesPage(Model model) {
        model.addAttribute("pageTitle", "Подбор видеоигр");
        return "games";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        model.addAttribute("pageTitle", "Личный кабинет");
        return "profile";
    }
}