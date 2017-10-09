/*********************************************************************************************************
 * Archivo que contine una clase abstracta con los metodos callbacks que pueden utilizar las hilos Asynctask
 * para poder modificar la activity Principal
 **********************************************************************************************************/

package com.example.esteban.android_code.ClienteUDP;

public interface InterfazAsyntask
{
    void mostrarToastMake(String msg    );
    void mostrarTextView(String msg);
}
