package com.billMate.frontend.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AuthController {

    public AuthController() {
        System.out.println("🟢 AuthController instanciado");
    }

    @GetMapping("/login")
    public String loginPage() {
        System.out.println("🟢 Llegó a /login");
        return "auth/login";
    }

    @GetMapping("/clientes")
    public String clientes() {
        System.out.println("🟢 Mostrando clientes");
        return "clientes";
    }

    @GetMapping("/facturas")
    public String facturas(){
        System.out.println("🟢 Mostrando facturas");
        return "facturas";
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        System.out.println("🟢 Mostrando dashboard");
        return "dashboard";
    }

    @GetMapping("/clientes/{clientId}/facturas")
    public String facturasCliente(@PathVariable Long clientId, Model model) {
        model.addAttribute("clientId", clientId);
        return "facturas-cliente";
    }

    @GetMapping("/facturas/{invoiceId}/editar")
    public String editarFactura(@PathVariable Long invoiceId, Model model) {
        model.addAttribute("invoiceId", invoiceId);
        return "factura-editar";
    }

    @GetMapping("/facturas/nueva/{clientId}")
    public String nuevaFactura(@PathVariable Long clientId, Model model) {
        model.addAttribute("clientId", clientId);
        return "factura-nueva";
    }

    @GetMapping("/usuarios")
    public String gestionUsuarios() {
        System.out.println("🟢 Mostrando pantalla de gestión de usuarios");
        return "auth/usuarios"; // Esto carga templates/usuarios.html
    }


}
