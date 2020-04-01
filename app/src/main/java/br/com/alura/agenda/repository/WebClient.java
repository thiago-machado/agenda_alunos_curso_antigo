package br.com.alura.agenda.repository;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WebClient {

    public String post(String json) {

        try {

            URL url = new URL("https://www.caelum.com.br/mobile");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            connection.setDoOutput(true);

            PrintStream saida = new PrintStream(connection.getOutputStream());
            saida.println(json);

            connection.connect();
            String resposta = new Scanner(connection.getInputStream()).next();


            return resposta;

        } catch (MalformedURLException murlex) {
            murlex.printStackTrace();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }

        return "FALHOU";
    }
}
