# 🎯 Instrucciones: Cómo Agregar Screenshots

## 📸 Imagen que Compartiste

Compartiste la imagen de **"Crear Cuenta"**. Esta debe guardarse con el nombre:

```
01_crear_cuenta.png
```

## 📂 Estructura Final Esperada

```
📁 AIA-Solutions/
├── 📁 screenshots/
│   ├── 01_crear_cuenta.png        ✅ (Imagen que compartiste)
│   ├── 02_login.png
│   ├── 03_inicio.png
│   ├── 04_business.png
│   ├── 05_turno_detail.png
│   ├── 06_perfil.png
│   ├── 07_config.png
│   ├── 08_admin_dashboard.png
│   ├── 09_admin_inicio.png
│   ├── 10_mi_negocio.png
│   ├── 11_admin_perfil.png
│   └── README.md
├── README.md
└── ...
```

## 🚀 Pasos para Completar

### 1️⃣ Guardar la Imagen que Compartiste

```
📍 Ubicación: c:\Cursos\AIA-Solutions\screenshots\01_crear_cuenta.png
```

**Cómo hacerlo:**

- Windows Explorer: Haz clic derecho en la imagen → "Guardar imagen como"
- Selecciona la carpeta `screenshots`
- Nombre: `01_crear_cuenta.png`

### 2️⃣ Capturar las Demás Pantallas

Usa uno de estos métodos:

**Android Studio:**

```
Tools → Screenshots → Capturar → Guardar en ./screenshots/
```

**ADB:**

```bash
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png ./screenshots/02_login.png
```

### 3️⃣ Verificar que Todo Esté Correcto

```bash
# Ver archivos en la carpeta
ls -la screenshots/

# Deberías ver:
# -rw-r--r-- ... 01_crear_cuenta.png
# -rw-r--r-- ... 02_login.png
# etc.
```

### 4️⃣ Actualizar el Repositorio

```bash
cd c:\Cursos\AIA-Solutions
git add screenshots/
git commit -m "Add application screenshots"
git push origin main
```

## ✨ Resultado Final

Una vez que hayas guardado todas las imágenes, el README mostrará una **galería visual hermosa** con todas las pantallas:

```
📸 Galería Visual de Pantallas

┌─────────────────────────────────────────────────────┐
│ Crear Cuenta  │  Login  │  Inicio  │  Búsqueda    │
│   [img]       │ [img]   │  [img]   │   [img]      │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│ Turno Detail  │ Perfil  │  Config  │  Admin Panel │
│   [img]       │ [img]   │  [img]   │   [img]      │
└─────────────────────────────────────────────────────┘
```

## ❓ ¿Necesitas Ayuda?

- **¿Cómo capturar screenshots?** → Ve a `screenshots/README.md`
- **¿ADB no funciona?** → Asegúrate que el emulador esté corriendo: `adb devices`
- **¿La imagen está muy grande?** → Comprime a ~300-400KB usando un compresor PNG online

---

**¡Listo! Una vez agregadas todas las imágenes, tu README tendrá una galería visual profesional.** 🎉
