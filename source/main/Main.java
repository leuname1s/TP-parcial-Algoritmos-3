package main;

import Funcionalidades.MenuConsola;
import Interfaces.IMenu;

public class Main {

    public static void main(String[] args) {
        IMenu menu = new MenuConsola();
        menu.ejecutarMenu();
    }
}