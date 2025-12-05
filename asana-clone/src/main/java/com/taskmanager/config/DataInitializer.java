package com.taskmanager.config;

import com.taskmanager.model.Role;
import com.taskmanager.model.User;
import com.taskmanager.model.Plan;
import com.taskmanager.Repositorios.RoleRepository;
import com.taskmanager.Repositorios.UserRepository;
import com.taskmanager.Repositorios.PlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Inicializador de Datos Unificado
 *
 * Carga datos iniciales en la base de datos al arrancar la aplicación:
 * - Roles del sistema (N°2)
 * - Usuario administrador por defecto (N°1, N°9)
 * - Usuarios de ejemplo
 * - Planes del sistema (Free, Pro, Business, Enterprise)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("Initializing application data...");
        log.info("========================================");

        initializeRoles();
        initializeAdminUser();
        initializeSampleUsers();
        initializePlans();

        log.info("========================================");
        log.info("Data initialization completed!");
        log.info("========================================");
    }

    // ====================================================
    // ROLES
    // ====================================================
    private void initializeRoles() {
        log.info("Checking roles...");

        for (Role.RoleType roleType : Role.RoleType.values()) {
            String roleName = roleType.name();

            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleType.getDescription())
                        .build();

                roleRepository.save(role);
                log.info("Created role: {} - {}", roleName, roleType.getDescription());
            }
        }
    }

    // ====================================================
    // ADMIN
    // ====================================================
    private void initializeAdminUser() {
        log.info("Checking admin user...");

        String adminEmail = "admin@asana.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(Role.RoleType.ROLE_ADMIN.name())
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found."));

            User admin = User.builder()
                    .email(adminEmail)
                    .firstName("Admin")
                    .lastName("System")
                    .password(passwordEncoder.encode("Admin123456"))
                    .roles(Set.of(adminRole))
                    .active(true)
                    .bio("Administrador del sistema")
                    .build();

            userRepository.save(admin);

            log.info("========================================");
            log.info("ADMIN USER CREATED");
            log.info("Email: {}", adminEmail);
            log.info("Password: Admin123456");
            log.info("========================================");
        }
    }

    // ====================================================
    // SAMPLE USERS
    // ====================================================
    private void initializeSampleUsers() {
        log.info("Creating sample users for testing...");

        createUserIfNotExists(
                "manager@asana.com",
                "María",
                "García",
                "Manager123456",
                Role.RoleType.ROLE_PROJECT_MANAGER
        );

        createUserIfNotExists(
                "member@asana.com",
                "Carlos",
                "López",
                "Member123456",
                Role.RoleType.ROLE_MEMBER
        );

        createUserIfNotExists(
                "viewer@asana.com",
                "Ana",
                "Martínez",
                "Viewer123456",
                Role.RoleType.ROLE_VIEWER
        );
    }

    private void createUserIfNotExists(
            String email,
            String firstName,
            String lastName,
            String password,
            Role.RoleType roleType) {

        if (!userRepository.existsByEmail(email)) {
            Role role = roleRepository.findByName(roleType.name())
                    .orElseThrow(() -> new IllegalStateException(roleType.name() + " not found"));

            User user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(passwordEncoder.encode(password))
                    .roles(Set.of(role))
                    .active(true)
                    .build();

            userRepository.save(user);

            log.info("Created sample user: {} ({}) - Password: {}",
                    email, roleType.name(), password);
        }
    }

    // ====================================================
    // PLANES
    // ====================================================
    private void initializePlans() {
        log.info("Checking plans...");

        if (planRepository.count() > 0) {
            log.info("Plans already initialized");
            return;
        }

        log.info("Initializing default plans...");

        // Plan Free
        Plan freePlan = Plan.builder()
                .name("Free")
                .description("Perfect for individuals getting started")
                .price(BigDecimal.ZERO)
                .annualPrice(BigDecimal.ZERO)
                .maxProjects(5)
                .maxMembers(5)
                .maxStorage(100)
                .maxAttachmentSize(5)
                .customFields(false)
                .timeline(false)
                .ganttChart(false)
                .advancedReports(false)
                .prioritySupport(false)
                .apiAccess(false)
                .customBranding(false)
                .ssoEnabled(false)
                .active(true)
                .trialDays(0)
                .build();

        // Plan Pro
        Plan proPlan = Plan.builder()
                .name("Pro")
                .description("For small teams that need more features")
                .price(BigDecimal.valueOf(15.00))
                .annualPrice(BigDecimal.valueOf(150.00))
                .maxProjects(50)
                .maxMembers(25)
                .maxStorage(10240)
                .maxAttachmentSize(50)
                .customFields(true)
                .timeline(true)
                .ganttChart(false)
                .advancedReports(true)
                .prioritySupport(false)
                .apiAccess(true)
                .customBranding(false)
                .ssoEnabled(false)
                .active(true)
                .trialDays(14)
                .build();

        // Plan Business
        Plan businessPlan = Plan.builder()
                .name("Business")
                .description("For growing teams that need advanced features")
                .price(BigDecimal.valueOf(39.00))
                .annualPrice(BigDecimal.valueOf(390.00))
                .maxProjects(-1)
                .maxMembers(100)
                .maxStorage(51200)
                .maxAttachmentSize(100)
                .customFields(true)
                .timeline(true)
                .ganttChart(true)
                .advancedReports(true)
                .prioritySupport(true)
                .apiAccess(true)
                .customBranding(true)
                .ssoEnabled(false)
                .active(true)
                .trialDays(14)
                .build();

        // Plan Enterprise
        Plan enterprisePlan = Plan.builder()
                .name("Enterprise")
                .description("For large organizations with custom needs")
                .price(BigDecimal.valueOf(99.00))
                .annualPrice(BigDecimal.valueOf(990.00))
                .maxProjects(-1)
                .maxMembers(-1)
                .maxStorage(-1)
                .maxAttachmentSize(500)
                .customFields(true)
                .timeline(true)
                .ganttChart(true)
                .advancedReports(true)
                .prioritySupport(true)
                .apiAccess(true)
                .customBranding(true)
                .ssoEnabled(true)
                .active(true)
                .trialDays(30)
                .build();

        planRepository.save(freePlan);
        planRepository.save(proPlan);
        planRepository.save(businessPlan);
        planRepository.save(enterprisePlan);

        log.info("Default plans initialized successfully");
    }
}
