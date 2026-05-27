# 📸 Screenshots - AIA Solutions

Esta carpeta contiene las capturas de pantalla de la aplicación móvil AIA Solutions.

## 📋 Pantallas Requeridas

### ✅ Pantallas de Cliente

| Nombre del Archivo    | Pantalla             | Estado       |
| --------------------- | -------------------- | ------------ |
| `01_crear_cuenta.png` | Crear Cuenta         | ✅ Agregada  |
| `02_login.png`        | Login                | ⏳ Pendiente |
| `03_inicio.png`       | Pantalla Principal   | ⏳ Pendiente |
| `04_business.png`     | Búsqueda de Negocios | ⏳ Pendiente |
| `05_turno_detail.png` | Detalle de Turno     | ⏳ Pendiente |
| `06_perfil.png`       | Perfil de Usuario    | ⏳ Pendiente |
| `07_config.png`       | Configuración        | ⏳ Pendiente |

### 👨‍💼 Pantallas de Administrador

| Nombre del Archivo       | Pantalla         | Estado       |
| ------------------------ | ---------------- | ------------ |
| `08_admin_dashboard.png` | Admin Dashboard  | ⏳ Pendiente |
| `09_admin_inicio.png`    | Panel de Control | ⏳ Pendiente |
| `10_mi_negocio.png`      | Mi Negocio       | ⏳ Pendiente |
| `11_admin_perfil.png`    | Perfil del Admin | ⏳ Pendiente |

## 🚀 Cómo Capturar Screenshots

### Opción 1: Android Studio (Recomendado)

1. Abre el proyecto en Android Studio
2. Ejecuta la app: `Shift+F10`
3. Ve a **Tools → Screenshot** (o `Ctrl+Shift+A` → "Screenshot")
4. Selecciona la resolución y captura
5. Guarda en esta carpeta con el nombre especificado

### Opción 2: ADB (Línea de Comando)

```bash
# Capturar screenshot
adb shell screencap -p /sdcard/screenshot.png

# Descargar a la computadora
adb pull /sdcard/screenshot.png ./screenshots/01_crear_cuenta.png
```

### Opción 3: Emulador Android

- Click derecho en el emulador → **Captura de pantalla**
- O presiona `Ctrl+Shift+A` en el emulador

## 📱 Recomendaciones

- **Resolución**: 1080 x 2340px
- **Formato**: PNG
- **Tamaño**: < 500KB
- **Orientación**: Vertical (Portrait)
- **Sin notificaciones**: Silencia notificaciones antes de capturar

## ✅ Próximos Pasos

1. Captura todas las pantallas
2. Guarda con los nombres especificados
3. Commit y push: `git add screenshots/ && git commit -m "Add app screenshots" && git push`
4. Las imágenes aparecerán automáticamente en el README

---

**Última actualización**: Mayo 2026
