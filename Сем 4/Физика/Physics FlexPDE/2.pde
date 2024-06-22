TITLE '3D Letter H'

COORDINATES
  CARTESIAN3

VARIABLES
  Temp

DEFINITIONS
  K = 100
  EnvTemp = 300

EQUATIONS
div(-K*grad(Temp)) = 10 * exp(-(x - 1.5) * (x - 1.5) - (y - 2.5) * (y - 2.5) - z * z)    

EXTRUSION
  SURFACE "Bottom"                    Z=0
    LAYER "Bottom Air"
  SURFACE "Top"                       Z=1
  
BOUNDARIES
REGION 1
  START(0,0)  
  natural(Temp) = -46 * (EnvTemp - Temp)
    LINE TO (1,0) TO (1,2) TO (2,2) TO (2,0) TO (3,0) TO (3,5) TO (2,5) TO (2,3) TO (1,3) TO (1,5) TO (0, 5) TO CLOSE


PLOTS
  GRID(x,y,z) AS "Elements grid"
     CONTOUR(Temp) ON  z=0 painted AS "Temperature field" 

END