import bpy

# Удаляем все объекты из сцены
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete(use_global=False)

# Создаем плоскость (пол)
bpy.ops.mesh.primitive_plane_add(size=10, location=(0, 0, 0))
floor = bpy.context.object
floor.name = "Floor"

# Создаем модель (куб)
bpy.ops.mesh.primitive_cube_add(size=2, location=(0, 0, 1))
cube = bpy.context.object
cube.name = "Cube"

# Добавляем источник света
bpy.ops.object.light_add(type='POINT', location=(2, 2, 5))
light = bpy.context.object
light.name = "PointLight"
light.data.energy = 1000  # Увеличиваем мощность света для четких теней

# Настраиваем камеру
bpy.ops.object.camera_add(location=(5, -5, 5), rotation=(1.2, 0, 0.9))
camera = bpy.context.object
camera.name = "Camera"

# Устанавливаем камеру как активную
bpy.context.scene.camera = camera

# Настраиваем рендеринг теней
bpy.context.scene.render.engine = 'CYCLES'  # Используем рендер Cycles для лучшего отображения теней
bpy.context.scene.cycles.samples = 128  # Количество сэмплов для рендера

# Опционально: Добавляем материал на пол
floor_mat = bpy.data.materials.new(name="FloorMaterial")
floor_mat.use_nodes = True
bsdf = floor_mat.node_tree.nodes["Principled BSDF"]
bsdf.inputs[0].default_value = (0.8, 0.8, 0.8, 1)  # Светло-серый цвет
floor.data.materials.append(floor_mat)

# Опционально: Добавляем материал на куб
cube_mat = bpy.data.materials.new(name="CubeMaterial")
cube_mat.use_nodes = True
bsdf = cube_mat.node_tree.nodes["Principled BSDF"]
bsdf.inputs[0].default_value = (0.2, 0.6, 0.8, 1)  # Голубой цвет
cube.data.materials.append(cube_mat)

print("Сцена готова! Перейдите в режим рендера для просмотра теней.")
