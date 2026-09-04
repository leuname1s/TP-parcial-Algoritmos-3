import funcionalidades.MenuConsola;
import interfaces.IMenu;

public class Main {

    public static void main(String[] args) {
        IMenu menu = new MenuConsola();
        menu.ejecutarMenu();
    }
}