# Импорт необходимых библиотек
import numpy as np          # Для работы с массивами (хранение изображения)
import math                 # Математические функции (sqrt, cos, sin, tan, pi)
import random               # Генерация случайных чисел (для выборки и антиалиасинга)
import time                 # Для измерения времени выполнения рендеринга
from dataclasses import dataclass, field  # Для создания классов с автоматической инициализацией
from typing import Optional, List, Tuple  # Аннотации типов для лучшей читаемости кода
import struct               # Для работы с бинарными данными (не используется в текущей версии)
from pathlib import Path    # Для работы с путями файлов (не используется в текущей версии)

# ====================================================================
# Базовые структуры данных
# ====================================================================

@dataclass
class Vec3:
    x: float
    y: float
    z: float

    def __add__(self, other):
        return Vec3(self.x + other.x, self.y + other.y, self.z + other.z)

    def __sub__(self, other):
        return Vec3(self.x - other.x, self.y - other.y, self.z - other.z)

    def __mul__(self, scalar):
        return Vec3(self.x * scalar, self.y * scalar, self.z * scalar)

    def __rmul__(self, scalar):
        return self * scalar

    def __truediv__(self, scalar):
        return Vec3(self.x / scalar, self.y / scalar, self.z / scalar)

    def __neg__(self):
        return Vec3(-self.x, -self.y, -self.z)

    def dot(self, other):
        return self.x * other.x + self.y * other.y + self.z * other.z

    def cross(self, other):
        return Vec3(
            self.y * other.z - self.z * other.y,
            self.z * other.x - self.x * other.z,
            self.x * other.y - self.y * other.x
        )

    def length(self):
        return math.sqrt(self.dot(self))

    def normalize(self):
        l = self.length()
        if l > 0:
            return self / l
        return self

    def __getitem__(self, index):
        if index == 0: return self.x
        if index == 1: return self.y
        if index == 2: return self.z
        raise IndexError("Index out of range")

@dataclass
class Color:
    r: float
    g: float
    b: float

    def __add__(self, other):
        return Color(self.r + other.r, self.g + other.g, self.b + other.b)

    def __sub__(self, other):
        return Color(self.r - other.r, self.g - other.g, self.b - other.b)

    def __mul__(self, other):
        if isinstance(other, Color):
            return Color(self.r * other.r, self.g * other.g, self.b * other.b)
        return Color(self.r * other, self.g * other, self.b * other)

    def __rmul__(self, scalar):
        return self * scalar

    def __truediv__(self, scalar):
        return Color(self.r / scalar, self.g / scalar, self.b / scalar)

    def clamp(self, min_val=0.0, max_val=1.0):
        return Color(
            max(min_val, min(max_val, self.r)),
            max(min_val, min(max_val, self.g)),
            max(min_val, min(max_val, self.b))
        )

    def to_rgb_bytes(self):
        return (
            int(max(0, min(1, self.r)) * 255),
            int(max(0, min(1, self.g)) * 255),
            int(max(0, min(1, self.b)) * 255)
        )

    @staticmethod
    def black():
        return Color(0, 0, 0)

    @staticmethod
    def white():
        return Color(1, 1, 1)

# ====================================================================
# Геометрия и лучи
# ====================================================================

@dataclass
class Ray:
    origin: Vec3
    direction: Vec3

    def point_at(self, t):
        return self.origin + self.direction * t

@dataclass
class Material:
    diffuse: Color
    specular: Color
    emission: Color = field(default_factory=lambda: Color(0, 0, 0))

    def is_light(self):
        return self.emission.r > 0 or self.emission.g > 0 or self.emission.b > 0

    def reflectance_sum(self):
        return Color(
            self.diffuse.r + self.specular.r,
            self.diffuse.g + self.specular.g,
            self.diffuse.b + self.specular.b
        )

    def is_physical(self):
        ref_sum = self.reflectance_sum()
        return ref_sum.r <= 1.0 and ref_sum.g <= 1.0 and ref_sum.b <= 1.0

@dataclass
class Triangle:
    v0: Vec3
    v1: Vec3
    v2: Vec3
    n0: Vec3
    n1: Vec3
    n2: Vec3
    material: Material
    emission: Color = field(default_factory=lambda: Color(0, 0, 0))

    def normal(self, u, v):
        w = 1 - u - v
        n = self.n0 * w + self.n1 * u + self.n2 * v
        return n.normalize()

    def intersect(self, ray, t_min=0.001):
        edge1 = self.v1 - self.v0
        edge2 = self.v2 - self.v0
        h = ray.direction.cross(edge2)
        a = edge1.dot(h)
        if abs(a) < 1e-8:
            return None
        f = 1.0 / a
        s = ray.origin - self.v0
        u = f * s.dot(h)
        if u < 0.0 or u > 1.0:
            return None
        q = s.cross(edge1)
        v = f * ray.direction.dot(q)
        if v < 0.0 or u + v > 1.0:
            return None
        t = f * edge2.dot(q)
        if t > t_min:
            return Intersection(
                t=t,
                point=ray.point_at(t),
                normal=self.normal(u, v),
                material=self.material,
                emission=self.emission,
                uv=(u, v)
            )
        return None

@dataclass
class Intersection:
    t: float
    point: Vec3
    normal: Vec3
    material: Material
    emission: Color
    uv: Tuple[float, float]

# ====================================================================
# Камера
# ====================================================================

class Camera:
    def __init__(self, position, target, up, fov, width, height):
        self.position = position
        self.target = target
        self.up = up
        self.fov = fov
        self.width = width
        self.height = height

        self.forward = (target - position).normalize()
        self.right = self.forward.cross(up).normalize()
        self.up = self.right.cross(self.forward).normalize()

        aspect_ratio = width / height
        self.half_height = math.tan(fov * 0.5 * math.pi / 180.0)
        self.half_width = aspect_ratio * self.half_height
        self.pixel_width = 2 * self.half_width / width
        self.pixel_height = 2 * self.half_height / height

    def generate_ray(self, x, y, offset_x=0.0, offset_y=0.0):
        px = ((x + offset_x) / self.width - 0.5) * 2 * self.half_width
        py = ((y + offset_y) / self.height - 0.5) * 2 * self.half_height
        direction = self.forward + self.right * px + self.up * py
        return Ray(self.position, direction.normalize())

# ====================================================================
# Сцена
# ====================================================================

class Scene:
    def __init__(self):
        self.triangles = []
        self.lights = []

    def add_triangle(self, triangle):
        self.triangles.append(triangle)
        if triangle.emission.r > 0 or triangle.emission.g > 0 or triangle.emission.b > 0:
            self.lights.append(triangle)

    def intersect(self, ray):
        closest = None
        for triangle in self.triangles:
            intersection = triangle.intersect(ray)
            if intersection and (closest is None or intersection.t < closest.t):
                closest = intersection
        return closest

# ====================================================================
# Трассировщик путей
# ====================================================================

class PathTracer:
    def __init__(self, scene, camera, width, height, samples_per_pixel=16, max_depth=5):
        self.scene = scene
        self.camera = camera
        self.width = width
        self.height = height
        self.samples_per_pixel = samples_per_pixel
        self.max_depth = max_depth
        self.image = np.zeros((height, width, 3), dtype=np.float32)

    def trace_path(self, ray, depth=0):
        if depth >= self.max_depth:
            return Color.black()

        intersection = self.scene.intersect(ray)
        if not intersection:
            return Color.black()

        # Если попали на источник света — всегда возвращаем его излучение
        if intersection.emission.r > 0 or intersection.emission.g > 0 or intersection.emission.b > 0:
            return intersection.emission

        material = intersection.material
        point = intersection.point
        normal = intersection.normal

        diffuse_strength = (material.diffuse.r + material.diffuse.g + material.diffuse.b) / 3.0
        specular_strength = (material.specular.r + material.specular.g + material.specular.b) / 3.0
        total_strength = diffuse_strength + specular_strength

        if total_strength < 1e-6:
            return Color.black()

        survival_probability = min(total_strength, 0.95)
        if random.random() > survival_probability:
            return Color.black()

        diffuse_prob = diffuse_strength / total_strength
        specular_prob = specular_strength / total_strength

        if random.random() < diffuse_prob:
            new_direction = self.random_cosine_weighted_hemisphere(normal)
            pdf = max(1e-8, new_direction.dot(normal) / math.pi)
            if pdf < 1e-12:
                return Color.black()
            new_ray = Ray(point + normal * 1e-4, new_direction)
            cosine = max(0.0, new_direction.dot(normal))
            incoming = self.trace_path(new_ray, depth + 1)
            bsdf = material.diffuse / math.pi
            result = incoming * bsdf * cosine / pdf
        else:
            reflected = ray.direction - normal * 2 * ray.direction.dot(normal)
            new_direction = reflected.normalize()
            new_ray = Ray(point + normal * 1e-4, new_direction)
            incoming = self.trace_path(new_ray, depth + 1)
            result = incoming * material.specular

        return result / survival_probability

    def random_cosine_weighted_hemisphere(self, normal):
        r = math.sqrt(random.random())
        theta = random.random() * 2 * math.pi
        x = r * math.cos(theta)
        y = r * math.sin(theta)
        z = math.sqrt(max(0.0, 1.0 - x*x - y*y))

        if abs(normal.x) > abs(normal.y):
            tangent = Vec3(normal.z, 0, -normal.x).normalize()
        else:
            tangent = Vec3(0, -normal.z, normal.y).normalize()

        bitangent = normal.cross(tangent)
        direction = tangent * x + bitangent * y + normal * z
        return direction.normalize()

    def sample_direct_lighting(self, point, normal):
        """Выборка прямого освещения от источников света (возвращает radiance contribution; BRDF применяется затем)"""
        if not self.scene.lights:
            return Color.black()

        light = random.choice(self.scene.lights)

        u = random.random()
        v = random.random()
        if u + v > 1:
            u = 1 - u
            v = 1 - v
        w = 1 - u - v

        light_point = light.v0 * w + light.v1 * u + light.v2 * v

        to_light = light_point - point
        distance = to_light.length()
        if distance <= 1e-8:
            return Color.black()
        light_dir = to_light / distance

        eps = 1e-4
        # Смещаем точку в направлении наружу от поверхности, чтобы избежать самопересечений
        origin = point + normal * eps if normal.dot(light_dir) > 0 else point - normal * eps

        shadow_ray = Ray(origin, light_dir)
        shadow_hit = self.scene.intersect(shadow_ray)
        if shadow_hit and shadow_hit.t < distance - 1e-4:
            return Color.black()

        neg_light_dir = -light_dir
        cos_light = max(0.0, light.normal(u, v).dot(neg_light_dir))
        cos_surface = max(0.0, normal.dot(light_dir))
        if cos_surface <= 0 or cos_light <= 0:
            return Color.black()

        edge1 = light.v1 - light.v0
        edge2 = light.v2 - light.v0
        light_area = edge1.cross(edge2).length() / 2.0
        if light_area <= 0:
            return Color.black()

        light_pdf = 1.0 / (len(self.scene.lights) * light_area)
        geometry_factor = cos_surface * cos_light / (distance * distance)
        if geometry_factor <= 0:
            return Color.black()

        return light.emission * (geometry_factor / light_pdf)

    def render(self):
        print(f"Начинаем рендеринг: {self.width}x{self.height}, {self.samples_per_pixel} сэмплов на пиксель")
        start_time = time.time()

        total_pixels = self.width * self.height
        processed_pixels = 0

        for y in range(self.height):
            for x in range(self.width):
                color = Color.black()

                for _ in range(self.samples_per_pixel):
                    offset_x = random.random() - 0.5
                    offset_y = random.random() - 0.5
                    ray = self.camera.generate_ray(x, y, offset_x, offset_y)
                    path_color = self.trace_path(ray)
                    intersection = self.scene.intersect(ray)

                    if intersection and not intersection.material.is_light():
                        direct_light = self.sample_direct_lighting(intersection.point, intersection.normal)
                        # Учитываем BRDF Ламберта: diffuse / pi
                        brdf = intersection.material.diffuse / math.pi
                        path_color = path_color + direct_light * brdf

                    color = color + path_color

                color = color / self.samples_per_pixel
                self.image[y, x] = [color.r, color.g, color.b]

                processed_pixels += 1
                if processed_pixels % 1000 == 0:
                    progress = processed_pixels / total_pixels * 100
                    print(f"Прогресс: {progress:.1f}%")

        end_time = time.time()
        print(f"Рендеринг завершен за {end_time - start_time:.2f} секунд")
        return self.image

    def tonemap_and_save(self, filename, gamma=2.2, exposure=1.0):
        print("Применяем тонмэппинг...")

        image_data = self.image * exposure

        max_brightness = np.max(image_data)
        avg_brightness = np.mean(image_data)

        print(f"Максимальная яркость до нормализации: {max_brightness}")
        print(f"Средняя яркость до нормализации: {avg_brightness}")

        if max_brightness > 0:
            image_data = image_data / max_brightness

        image_data = np.clip(image_data, 0.0, 1.0)
        image_data = np.power(image_data, 1.0 / gamma)
        image_8bit = (image_data * 255).astype(np.uint8)
        self.save_ppm(filename, image_8bit)
        print(f"Изображение сохранено в {filename}")

    def save_ppm(self, filename, image_data):
        height, width, _ = image_data.shape
        with open(filename, 'wb') as f:
            f.write(f"P6\n{width} {height}\n255\n".encode())
            for y in range(height):
                for x in range(width):
                    f.write(bytes(image_data[y, x]))

# ====================================================================
# Создание тестовой сцены (Cornell box)
# ====================================================================

def create_cornell_box():
    scene = Scene()

    red_material = Material(diffuse=Color(0.65, 0.05, 0.05), specular=Color(0.0, 0.0, 0.0))
    green_material = Material(diffuse=Color(0.12, 0.45, 0.15), specular=Color(0.0, 0.0, 0.0))
    white_material = Material(diffuse=Color(0.73, 0.73, 0.73), specular=Color(0.0, 0.0, 0.0))
    mirror_material = Material(diffuse=Color(0.0, 0.0, 0.0), specular=Color(0.98, 0.98, 0.98))
    light_material = Material(diffuse=Color(0.0, 0.0, 0.0), specular=Color(0.0, 0.0, 0.0), emission=Color(15, 15, 15))

    # Пол
    scene.add_triangle(Triangle(
        Vec3(-1, -1, -1), Vec3(1, -1, -1), Vec3(-1, -1, 1),
        Vec3(0, 1, 0), Vec3(0, 1, 0), Vec3(0, 1, 0),
        white_material, Color(0, 0, 0)
    ))
    scene.add_triangle(Triangle(
        Vec3(1, -1, -1), Vec3(1, -1, 1), Vec3(-1, -1, 1),
        Vec3(0, 1, 0), Vec3(0, 1, 0), Vec3(0, 1, 0),
        white_material, Color(0, 0, 0)
    ))

    # Потолок
    scene.add_triangle(Triangle(
        Vec3(-1, 1, -1), Vec3(-1, 1, 1), Vec3(1, 1, -1),
        Vec3(0, -1, 0), Vec3(0, -1, 0), Vec3(0, -1, 0),
        white_material, Color(0, 0, 0)
    ))
    scene.add_triangle(Triangle(
        Vec3(1, 1, -1), Vec3(-1, 1, 1), Vec3(1, 1, 1),
        Vec3(0, -1, 0), Vec3(0, -1, 0), Vec3(0, -1, 0),
        white_material, Color(0, 0, 0)
    ))

    # Источник на потолке (маленький прямоугольник)
    light_size = 0.4
    scene.add_triangle(Triangle(
        Vec3(-light_size, 0.99, -light_size),
        Vec3(-light_size, 0.99, light_size),
        Vec3(light_size, 0.99, -light_size),
        Vec3(0, -1, 0), Vec3(0, -1, 0), Vec3(0, -1, 0),
        light_material, Color(15, 15, 15)
    ))
    scene.add_triangle(Triangle(
        Vec3(light_size, 0.99, -light_size),
        Vec3(-light_size, 0.99, light_size),
        Vec3(light_size, 0.99, light_size),
        Vec3(0, -1, 0), Vec3(0, -1, 0), Vec3(0, -1, 0),
        light_material, Color(15, 15, 15)
    ))

    # Левая стена (красная)
    scene.add_triangle(Triangle(
        Vec3(-1, -1, -1), Vec3(-1, -1, 1), Vec3(-1, 1, -1),
        Vec3(1, 0, 0), Vec3(1, 0, 0), Vec3(1, 0, 0),
        red_material, Color(0, 0, 0)
    ))
    scene.add_triangle(Triangle(
        Vec3(-1, 1, -1), Vec3(-1, -1, 1), Vec3(-1, 1, 1),
        Vec3(1, 0, 0), Vec3(1, 0, 0), Vec3(1, 0, 0),
        red_material, Color(0, 0, 0)
    ))

    # Правая стена (зелёная)
    scene.add_triangle(Triangle(
        Vec3(1, -1, -1), Vec3(1, 1, -1), Vec3(1, -1, 1),
        Vec3(-1, 0, 0), Vec3(-1, 0, 0), Vec3(-1, 0, 0),
        green_material, Color(0, 0, 0)
    ))
    scene.add_triangle(Triangle(
        Vec3(1, 1, -1), Vec3(1, 1, 1), Vec3(1, -1, 1),
        Vec3(-1, 0, 0), Vec3(-1, 0, 0), Vec3(-1, 0, 0),
        green_material, Color(0, 0, 0)
    ))

    # Задняя стена
    scene.add_triangle(Triangle(
        Vec3(-1, -1, 1), Vec3(1, -1, 1), Vec3(-1, 1, 1),
        Vec3(0, 0, -1), Vec3(0, 0, -1), Vec3(0, 0, -1),
        white_material, Color(0, 0, 0)
    ))
    scene.add_triangle(Triangle(
        Vec3(1, -1, 1), Vec3(1, 1, 1), Vec3(-1, 1, 1),
        Vec3(0, 0, -1), Vec3(0, 0, -1), Vec3(0, 0, -1),
        white_material, Color(0, 0, 0)
    ))

    # Функция для добавления сферы (аппрокс. треугольной сеткой)
    def add_sphere(center, radius, material, scene, subdivisions=1):
        t = (1.0 + math.sqrt(5.0)) / 2.0
        vertices = [
            Vec3(-1, t, 0), Vec3(1, t, 0), Vec3(-1, -t, 0), Vec3(1, -t, 0),
            Vec3(0, -1, t), Vec3(0, 1, t), Vec3(0, -1, -t), Vec3(0, 1, -t),
            Vec3(t, 0, -1), Vec3(t, 0, 1), Vec3(-t, 0, -1), Vec3(-t, 0, 1)
        ]
        vertices = [v.normalize() for v in vertices]
        faces = [
            (0, 11, 5), (0, 5, 1), (0, 1, 7), (0, 7, 10), (0, 10, 11),
            (1, 5, 9), (5, 11, 4), (11, 10, 2), (10, 7, 6), (7, 1, 8),
            (3, 9, 4), (3, 4, 2), (3, 2, 6), (3, 6, 8), (3, 8, 9),
            (4, 9, 5), (2, 4, 11), (6, 2, 10), (8, 6, 7), (9, 8, 1)
        ]

        def subdivide(v1, v2, v3, depth):
            if depth == 0:
                p0 = center + v1 * radius
                p1 = center + v2 * radius
                p2 = center + v3 * radius
                n0 = v1.normalize()
                n1 = v2.normalize()
                n2 = v3.normalize()
                scene.add_triangle(Triangle(p0, p1, p2, n0, n1, n2, material, Color(0, 0, 0)))
            else:
                v12 = (v1 + v2).normalize()
                v23 = (v2 + v3).normalize()
                v31 = (v3 + v1).normalize()
                subdivide(v1, v12, v31, depth - 1)
                subdivide(v2, v23, v12, depth - 1)
                subdivide(v3, v31, v23, depth - 1)
                subdivide(v12, v23, v31, depth - 1)

        for face in faces:
            v1 = vertices[face[0]]
            v2 = vertices[face[1]]
            v3 = vertices[face[2]]
            subdivide(v1, v2, v3, subdivisions)

    add_sphere(Vec3(-0.4, -0.7, 0.2), 0.3, mirror_material, scene, subdivisions=1)
    add_sphere(Vec3(0.4, -0.7, -0.2), 0.3, white_material, scene, subdivisions=1)

    return scene

# ====================================================================
# Интерфейс пользователя и утилиты
# ====================================================================

class RenderSettings:
    def __init__(self):
        self.width = 400
        self.height = 400
        self.samples_per_pixel = 16
        self.max_depth = 5
        self.fov = 45
        self.camera_position = Vec3(0, 0, -3)
        self.camera_target = Vec3(0, 0, 0)
        self.camera_up = Vec3(0, 1, 0)
        self.exposure = 1.0
        self.gamma = 2.2

    def create_camera(self):
        return Camera(self.camera_position, self.camera_target, self.camera_up, self.fov, self.width, self.height)

    def print_settings(self):
        print("\n" + "="*60)
        print("ТЕКУЩИЕ НАСТРОЙКИ РЕНДЕРИНГА:")
        print("="*60)
        print(f"Разрешение: {self.width}x{self.height}")
        print(f"Сэмплов на пиксель: {self.samples_per_pixel}")
        print(f"Глубина трассировки: {self.max_depth}")
        print(f"Поле зрения: {self.fov} градусов")
        print(f"Положение камеры: ({self.camera_position.x:.2f}, {self.camera_position.y:.2f}, {self.camera_position.z:.2f})")
        print(f"Направление камеры: ({self.camera_target.x:.2f}, {self.camera_target.y:.2f}, {self.camera_target.z:.2f})")
        print(f"Экспозиция: {self.exposure}")
        print(f"Гамма-коррекция: {self.gamma}")
        print("="*60)

def print_menu():
    print("\n" + "="*60)
    print("ПАТЧ-ТРЕЙСЕР С ГЛОБАЛЬНЫМ ОСВЕЩЕНИЕМ")
    print("="*60)
    print("1. Быстрый тест (200x200, 4 сэмпла)")
    print("2. Среднее качество (400x400, 16 сэмплов)")
    print("3. Высокое качество (600x600, 64 сэмпла)")
    print("4. Рендеринг с текущими настройками")
    print("="*40)
    print("ИЗМЕНИТЬ НАСТРОЙКИ:")
    print("5. Изменить разрешение")
    print("6. Изменить количество сэмплов")
    print("7. Изменить глубину трассировки")
    print("8. Изменить положение камеры")
    print("9. Изменить поле зрения")
    print("10. Изменить экспозицию")
    print("="*40)
    print("11. Показать текущие параметры")
    print("12. Выход")
    print("="*60)

def safe_input_float(prompt, default=None, min_val=None, max_val=None):
    while True:
        try:
            value_str = input(prompt)
            if not value_str and default is not None:
                return default
            value = float(value_str)
            if min_val is not None and value < min_val:
                print(f"Ошибка: значение должно быть не меньше {min_val}")
                continue
            if max_val is not None and value > max_val:
                print(f"Ошибка: значение должно быть не больше {max_val}")
                continue
            return value
        except ValueError:
            print("Ошибка: введите число")
        except KeyboardInterrupt:
            print("\nПрервано пользователем")
            return None

def safe_input_int(prompt, default=None, min_val=None, max_val=None):
    while True:
        try:
            value_str = input(prompt)
            if not value_str and default is not None:
                return default
            value = int(value_str)
            if min_val is not None and value < min_val:
                print(f"Ошибка: значение должно быть не меньше {min_val}")
                continue
            if max_val is not None and value > max_val:
                print(f"Ошибка: значение должно быть не больше {max_val}")
                continue
            return value
        except ValueError:
            print("Ошибка: введите целое число")
        except KeyboardInterrupt:
            print("\nПрервано пользователем")
            return None

def change_resolution(settings):
    print(f"\nТекущее разрешение: {settings.width}x{settings.height}")
    width = safe_input_int("Введите ширину (100-800): ", settings.width, 100, 800)
    if width is None:
        return settings
    height = safe_input_int("Введите высоту (100-800): ", settings.height, 100, 800)
    if height is None:
        return settings
    settings.width = width
    settings.height = height
    print(f"Разрешение изменено на {settings.width}x{settings.height}")
    return settings

def change_samples(settings):
    print(f"\nТекущее количество сэмплов на пиксель: {settings.samples_per_pixel}")
    samples = safe_input_int("Введите количество сэмплов (1-512): ", settings.samples_per_pixel, 1, 512)
    if samples is None:
        return settings
    settings.samples_per_pixel = samples
    print(f"Количество сэмплов изменено на {settings.samples_per_pixel}")
    return settings

def change_max_depth(settings):
    print(f"\nТекущая глубина трассировки: {settings.max_depth}")
    depth = safe_input_int("Введите глубину трассировки (1-10): ", settings.max_depth, 1, 10)
    if depth is None:
        return settings
    settings.max_depth = depth
    print(f"Глубина трассировки изменена на {settings.max_depth}")
    return settings

def change_camera_position(settings):
    print(f"\nТекущее положение камеры: ({settings.camera_position.x:.2f}, {settings.camera_position.y:.2f}, {settings.camera_position.z:.2f})")
    print("Рекомендуемые значения для Корнелл бокса:")
    print("  X: от -2 до 2")
    print("  Y: от -2 до 2")
    print("  Z: от -5 до -1 (отрицательные значения - камера смотрит на сцену)")
    x = safe_input_float("Введите X координату: ", settings.camera_position.x)
    if x is None:
        return settings
    y = safe_input_float("Введите Y координату: ", settings.camera_position.y)
    if y is None:
        return settings
    z = safe_input_float("Введите Z координату: ", settings.camera_position.z)
    if z is None:
        return settings
    settings.camera_position = Vec3(x, y, z)
    print(f"Положение камеры изменено на ({x:.2f}, {y:.2f}, {z:.2f})")
    return settings

def change_fov(settings):
    print(f"\nТекущее поле зрения: {settings.fov} градусов")
    print("Рекомендуемые значения: 30-90 градусов")
    fov = safe_input_float("Введите поле зрения (10-120 градусов): ", settings.fov, 10, 120)
    if fov is None:
        return settings
    settings.fov = fov
    print(f"Поле зрения изменено на {settings.fov} градусов")
    return settings

def change_exposure(settings):
    print(f"\nТекущая экспозиция: {settings.exposure}")
    print("Рекомендуемые значения: 0.5-2.0")
    exposure = safe_input_float("Введите экспозицию (0.1-5.0): ", settings.exposure, 0.1, 5.0)
    if exposure is None:
        return settings
    settings.exposure = exposure
    print(f"Экспозиция изменена на {settings.exposure}")
    return settings

def render_with_settings(scene, settings):
    print(f"\nНачинаем рендеринг с параметрами:")
    settings.print_settings()
    print("\nПредупреждение: рендеринг может занять значительное время!")
    print("Для прерывания нажмите Ctrl+C")
    try:
        camera = settings.create_camera()
        tracer = PathTracer(scene, camera, settings.width, settings.height,
                            settings.samples_per_pixel, settings.max_depth)
        image = tracer.render()
        timestamp = time.strftime("%Y%m%d_%H%M%S")
        filename = f"render_{settings.width}x{settings.height}_spp{settings.samples_per_pixel}_{timestamp}.ppm"
        tracer.tonemap_and_save(filename, exposure=settings.exposure, gamma=settings.gamma)
        print(f"\n✓ Рендеринг завершен! Изображение сохранено как: {filename}")
        print("  Для просмотра используйте программы типа IrfanView, GIMP или конвертируйте в PNG.")
        return tracer
    except KeyboardInterrupt:
        print("\n✗ Рендеринг прерван пользователем")
        return None
    except Exception as e:
        print(f"\n✗ Ошибка при рендеринге: {e}")
        return None

def main():
    print("="*60)
    print("ПАТЧ-ТРЕЙСЕР С ГЛОБАЛЬНЫМ ОСВЕЩЕНИЕМ")
    print("="*60)
    print("Инициализация...")
    scene = create_cornell_box()
    print(f"✓ Сцена создана: {len(scene.triangles)} треугольников, {len(scene.lights)} источников света")
    settings = RenderSettings()
    while True:
        print_menu()
        choice_input = input("\nВыберите опцию (1-12): ").strip()
        if not choice_input:
            print("Ошибка: введите номер опции")
            continue
        try:
            choice = int(choice_input)
        except ValueError:
            print("Ошибка: введите число от 1 до 12")
            continue

        if choice == 1:
            print("\nУстановка параметров для быстрого теста...")
            settings.width = 200
            settings.height = 200
            settings.samples_per_pixel = 4
            settings.max_depth = 3
            settings.exposure = 1.5
            render_with_settings(scene, settings)
        elif choice == 2:
            print("\nУстановка параметров для среднего качества...")
            settings.width = 400
            settings.height = 400
            settings.samples_per_pixel = 16
            settings.max_depth = 5
            settings.exposure = 1.0
            render_with_settings(scene, settings)
        elif choice == 3:
            print("\nУстановка параметров для высокого качества...")
            print("Внимание: это может занять несколько минут!")
            settings.width = 600
            settings.height = 600
            settings.samples_per_pixel = 64
            settings.max_depth = 8
            settings.exposure = 1.0
            render_with_settings(scene, settings)
        elif choice == 4:
            print("\nРендеринг с текущими настройками...")
            render_with_settings(scene, settings)
        elif choice == 5:
            settings = change_resolution(settings)
        elif choice == 6:
            settings = change_samples(settings)
        elif choice == 7:
            settings = change_max_depth(settings)
        elif choice == 8:
            settings = change_camera_position(settings)
        elif choice == 9:
            settings = change_fov(settings)
        elif choice == 10:
            settings = change_exposure(settings)
        elif choice == 11:
            settings.print_settings()
        elif choice == 12:
            print("\nВыход из программы...")
            break
        else:
            print("Неверный выбор. Введите число от 1 до 12")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nПрограмма завершена пользователем")
    except Exception as e:
        print(f"\nНеожиданная ошибка: {e}")