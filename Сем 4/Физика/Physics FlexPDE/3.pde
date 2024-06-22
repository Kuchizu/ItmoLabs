{ BENTBAR_MOVING.PDE

      

  This problem is a moving mesh variant of BENTBAR.PDE

}

 

title "Timoshenko's Bar with end load"

 

variables

    U(1e-6)           { X-displacement }

    V (1e-6)         { Y-displacement }

    Xm = move(x)

    Ym = move(y)

 

definitions

    L = 1               { Bar length }

    hL = L/2

    W = 0.1             { Bar thickness }

    hW = W/2

    eps = 0.01*L

    I = 2*hW^3/3       { Moment of inertia }

 

    nu = 0.3           { Poisson's Ratio }

    E  = 2.0e11         { Young's Modulus for Steel (N/M^2) }

                       { plane stress coefficients }

    G  = E/(1-nu^2)

    C11 = G

    C12 = G*nu

    C22 = G

    C33 = G*(1-nu)/2

 

    amplitude=GLOBALMAX(abs(v)) { for grid-plot scaling }

    mag=1/(amplitude+1e-6)

 

    force = -250         { total loading force in Newtons (~10 pound force) }

    dist = 0.5*force*(hW^2-y^2)/I       { Distributed load }

 

    Sx = (C11*dx(U) + C12*dy(V))       { Stresses }

    Sy = (C12*dx(U) + C22*dy(V))

    Txy = C33*(dy(U) + dx(V))

 

   { Timoshenko's analytic solution:  }

    Vexact = (force/(6*E*I))*((L-x)^2*(2*L+x) + 3*nu*x*y^2)

    Uexact = (force/(6*E*I))*(3*y*(L^2-x^2) +(2+nu)*y^3 -6*(1+nu)*hW^2*y)

    Sxexact = -force*x*y/I

    Txyexact = -0.5*force*(hW^2-y^2)/I

 

initial values

    U = 0

    V = 0

 

equations             { the displacement equations }

! force the displacements to evolve in pseudo-time, to allow a smooth deformation of the mesh.

! the time scale of these equations is arbitrary

    U:  dx(Sx) + dy(Txy) = dt(U)

    V:  dx(Txy) + dy(Sy) = dt(v)

! the mesh surrogate variables.  They move at the same rate as the material deformation

    Xm: dt(Xm) = dt(U)

    Ym: dt(Ym) = dt(V)

 

 

boundaries

   region 1

     start (0,-hW)

 

     load(U)=0         { free boundary on bottom, no normal stress }

     load(V)=0

       line to (L,-hW)

 

     value(U) = Uexact { clamp the right end }

     mesh_spacing=hW/10

       line to (L,0) point value(V) = 0

       line to (L,hW)

 

     load(U)=0         { free boundary on top, no normal stress }

     load(V)=0

     mesh_spacing=10

       line to (0,hW)

 

     load(U) = 0

     load(V) = dist   { apply distributed load to Y-displacement equation }

       line to close

 

time 0 to 1e-8 !by 1e-10

 

plots

for cycle=1

   ! x and y have already been moved by u and v, but this is small compared to mag*u, etc.

   grid(x+mag*U,y+mag*V)   as "deformation"   { show final deformed grid }

   elevation(V,Vexact) from(0,0) to (L,0) as "Center Y-Displacement(M)"

   elevation(V,Vexact) from(0,hW) to (L,hW) as "Top Y-Displacement(M)"

   elevation(U,Uexact) from(0,hW) to (L,hW) as "Top X-Displacement(M)"

   elevation(Sx,Sxexact) from(0,hW) to (L,hW) as "Top X-Stress"

   elevation(Txy,Txyexact) from(0,0) to (L,0) as "Center Shear Stress"

 

end

{

    Этот код описывает процесс моделирования деформации упругой балки под действием сосредоточенной нагрузки на конце. Тут используются принципы теории упругости для изучения деформации в материалах.

    В этом коде рассматривается задача деформации упругой балки Тимошенко под действием сосредоточенной нагрузки на конце. Сначала задаются характеристики балки, такие как ее размеры, модуль Юнга, коэффициент Пуассона, и другие параметры. Затем определяются переменные для смещений вдоль осей X и Y, а также переменные для перемещений узлов сетки, чтобы описать, как должна деформироваться сетка вместе с материалом. Затем формулируется уравнения для расчёта напряжений:

    Sx = (C11 * dx(U) + C12 * dy(V));
    Sy = (C12 * dx(U) + C22 * dy(V));
    Txy = C33 * (dy(U) + dx(V)).

    Устанавливаются граничные условия для различных границ бруса, например смещение и распределение нагрузок. Методом конечных элементов, используемым в FlexPDE, производится численное решение дифференциальных уравнений для определения неизвестных смещений и напряжений. Через установленный временной интервал и шаги происходит отслеживание эволюции состояния бруса, тут используется псевдовремя для стабилизации решения.

}