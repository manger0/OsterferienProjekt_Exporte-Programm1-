package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    // (Export) program1 Oster Aufgabe Mathias Angerer

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;
        int choice = 1;
        try {
            String url = "jdbc:mysql://localhost:3306/program1?user=root";
            connection = DriverManager.getConnection(url);
            System.out.println("\n(Export) program1");
            while (choice != 0) {
                System.out.println("1. printing OrderDate; CustomerId; totalPrice");
                System.out.println("2. printing ingredient; ingredientAmount");
                System.out.println("\ntype in (number of action) or (0) for end");
                choice = scanner.nextInt();

                // (export1) printing OrderDate; CustomerId; totalPrice
                if (choice == 1) export1(connection);

                // (export2) printing ingredient; ingredientAmount
                if (choice == 2) export2(connection);
            }
        } catch (SQLException | IOException e) {
            throw new Error("connection problem", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void export1(Connection connection) {
        // creating and if allready exists emptying existing file
        File myFile = new File("C:\\Users\\DCV\\Desktop\\BestellId_KundeNr_Gesamtpreis.csv");
        if (myFile.exists()) myFile.delete();

        String query = "select order_date, customer_id, delivery_price + total_meal_price + extra_price AS total_price" +
                " from `order` ORDER BY order_date DESC";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            System.out.println("OrderDate      | CustomerId | totalPrice");
            while (resultSet.next()) {
                Date date = resultSet.getDate("order_date");
                int customerId = resultSet.getInt("customer_id");
                double totalPrice = resultSet.getDouble("total_price");
                export1ToFile(myFile, date, customerId, totalPrice);
                System.out.println(date + "     |     " + customerId + "     |     " + totalPrice + "$");
            }
        } catch (SQLException | IOException e) {
            System.out.println("export1 problem");
        }
        System.out.println("\n");
    }

    public static void export1ToFile(File myFile, Date date, int customerId, double totalPrice) throws IOException {
        FileWriter myWriter = new FileWriter(myFile, true);
        myWriter.write("OrderDate      | CustomerId | totalPrice\n");
        myWriter.write(date + "     |     " + customerId + "     |     " + totalPrice + "$");
        myWriter.write("\n\n");
        myWriter.flush();
        myWriter.close();
    }

    public static void export2(Connection connection) throws IOException {
        // creating and if allready exists emptying existing file
        File myFile = new File("C:\\Users\\DCV\\Desktop\\Zutat_Anzahl.csv");
        if (myFile.exists()) myFile.delete();
        ArrayList<String> ingredients = createIngredientArray(connection);
        int[] ingredientCounter = new int[ingredients.size()];
        ingredientCounter = ingredientCount(ingredients, ingredientCounter, connection);
        // printing of ingredient and amount
        printIngredientsList(ingredients, ingredientCounter);
        export2ToFile(ingredients, ingredientCounter, myFile);
    }

    public static void export2ToFile(ArrayList<String> ingredients, int[] ingredientCounter, File myFile) throws IOException {
        for (int i = 0; i < ingredients.size(); i++) {
            FileWriter myWriter = new FileWriter(myFile, true);
            if (i == 0) myWriter.write("Zutat      |      Anzahl\n");
            myWriter.write(ingredients.get(i) + "      |    (" + ingredientCounter[i] + ")\n");
            myWriter.flush();
            myWriter.close();
        }
    }

    public static void printIngredientsList(ArrayList<String> ingredients, int[] ingredientCounter) {
        System.out.println("Zutat      |      Anzahl");
        for (int i = 0; i < ingredients.size(); i++) {
            System.out.println(ingredients.get(i) + "      |    (" + ingredientCounter[i] + ")");
        }
        System.out.println("\n");
    }


    public static int[] ingredientCount(ArrayList<String> ingredients, int[] ingredientCounter, Connection connection) {
        String query = "select * from meals_total";
        // get meal from meals_total table
        try (Statement statementMeal = connection.createStatement()) {
            ResultSet resultSetMeal = statementMeal.executeQuery(query);
            while (resultSetMeal.next()) {
                String meal = resultSetMeal.getString("meal");
                // getting all ingredients from meal
                for (int i = 1; i <= 10; i++) {
                    String printIngredient = "SELECT ingredients.ingredient_name\n" +
                            "FROM menu\n" +
                            "INNER JOIN ingredients ON menu.ingredient" + i + " = ingredients.ingredient_id\n" +
                            "WHERE menu_name = '" + meal + "';";
                    try (Statement statementRead = connection.createStatement()) {
                        ResultSet resultSet = statementRead.executeQuery(printIngredient);
                        String ingredient = "";
                        while (resultSet.next()) {
                            ingredient = resultSet.getString(1);
                            // increment ingredientCounter Array
                            if (!ingredient.equalsIgnoreCase("")) {
                                for (int j = 0; j < ingredients.size(); j++) {
                                    if (ingredient.equalsIgnoreCase(ingredients.get(j))) {
                                        ingredientCounter[j]++;
                                    }
                                }
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("ingredient print problem");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("wrong input");
        }
        return ingredientCounter;
    }


    public static ArrayList<String> createIngredientArray(Connection connection) {
        ArrayList<String> ingredients = new ArrayList<>();
        String query = "select * from ingredients";
        try (Statement statementRead = connection.createStatement()) {
            ResultSet resultSet = statementRead.executeQuery(query);
            while (resultSet.next()) {
             String ingredient = resultSet.getString("ingredient_name");
             ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            System.out.println("createIngredientArray problem");
        }
        return ingredients;
    }
}
