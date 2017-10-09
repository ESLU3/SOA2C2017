/*********************************************************************************************************
 * Archivo que contine una clase abstracta con los metodos callbacks que pueden utilizar las hilos Asynctask
 * para poder modificar la activity Principal
 **********************************************************************************************************/
package com.example.esteban.android_http.ClienteHttp;

public interface InterfazAsyntask
{
    void mostrarToastMake(String msg);
    void mostrarTextViewPotenciometro(String msg);
    void actualizarVelocimetro(float valor);
}
