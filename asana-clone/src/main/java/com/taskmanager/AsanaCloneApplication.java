package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase Principal de la Aplicación Asana Clone
 *
 * SPRINTS COMPLETADOS:
 *
 * SPRINT 1:
 * ✅ N°1: Autenticación de usuarios (JWT + Spring Security)
 * ✅ N°2: Gestión de roles (4 roles: ADMIN, PROJECT_MANAGER, MEMBER, VIEWER)
 * ✅ N°3: Creación de proyectos (CRUD completo)
 * ✅ N°4: Asignación de colaboradores (ManyToMany)
 * ✅ N°9: Gestión de usuarios (CRUD completo)
 *
 * SPRINT 2:
 * ✅ N°5: Gestión de procesos (Columnas Kanban)
 * ✅ N°6: Estados de tareas (6 estados + 4 prioridades)
 * ✅ N°18: Subtareas (Jerarquía de tareas)
 * ✅ N°10: Comentarios en tareas (Con hilos)
 * ✅ N°11: Adjuntar archivos (Upload/download)
 *
 * SPRINT 3:
 * ✅ N°7: Seguimiento de avances (Timeline + Reportes)
 * ✅ N°8: Notificaciones internas (18 tipos)
 * ✅ N°13: Filtros y búsqueda avanzada (Paginación)
 * ✅ N°14: Chat del proyecto (Tiempo real + Reacciones)
 * ✅ N°15: Configuración de notificaciones (Personalizable)
 *
 * CARACTERÍSTICAS TÉCNICAS:
 * - Spring Boot 3.2.0
 * - Java 17
 * - Spring Security con JWT
 * - Spring Data JPA (Query Methods, sin SQL)
 * - Lombok (reduce boilerplate)
 * - MapStruct (mapeo automático)
 * - H2 Database (desarrollo)
 * - MySQL (producción)
 *
 * @SpringBootApplication: Combina tres anotaciones:
 * - @Configuration: Define beans de configuración
 * - @EnableAutoConfiguration: Configuración automática de Spring
 * - @ComponentScan: Escanea componentes en el paquete y subpaquetes
 */
@SpringBootApplication
public class AsanaCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsanaCloneApplication.class, args);

		System.out.println("\n========================================");
		System.out.println("🚀 ASANA CLONE - SPRINT 1 STARTED");
		System.out.println("========================================");
		System.out.println("📊 H2 Console: http://localhost:8080/h2-console");
		System.out.println("   JDBC URL: jdbc:h2:mem:asanadb");
		System.out.println("   Username: sa");
		System.out.println("   Password: (blank)");
		System.out.println("========================================");
		System.out.println("🔐 Default Users:");
		System.out.println("   Admin:   admin@asana.com / Admin123456");
		System.out.println("   Manager: manager@asana.com / Manager123456");
		System.out.println("   Member:  member@asana.com / Member123456");
		System.out.println("   Viewer:  viewer@asana.com / Viewer123456");
		System.out.println("========================================");
		System.out.println("📡 API Endpoints:");
		System.out.println("   POST   /api/auth/register");
		System.out.println("   POST   /api/auth/login");
		System.out.println("   GET    /api/auth/me");
		System.out.println("   GET    /api/users");
		System.out.println("   GET    /api/users/{id}");
		System.out.println("   POST   /api/projects");
		System.out.println("   GET    /api/projects");
		System.out.println("   POST   /api/projects/{id}/members/{userId}");
		System.out.println("========================================");
		System.out.println("✅ Sprints Completados:");
		System.out.println("   Sprint 1: Auth, Roles, Proyectos, Usuarios");
		System.out.println("   Sprint 2: Procesos Kanban, Tareas, Comentarios, Archivos");
		System.out.println("   Sprint 3: Notificaciones, Chat, Timeline, Filtros");
		System.out.println("========================================");
		System.out.println("📊 Estadísticas del Sistema:");
		System.out.println("   Total Entidades: 12");
		System.out.println("   Total Endpoints API: ~145");
		System.out.println("   Total Query Methods: 120+");
		System.out.println("   Total Servicios: 13");
		System.out.println("========================================\n");
	}
}