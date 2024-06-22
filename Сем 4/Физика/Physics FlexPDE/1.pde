{ Fill in the following sections (removing comment marks ! if necessary),
  and delete those that are unused.}
TITLE 'Laba 1'     { the problem identification }
COORDINATES cartesian2  { coordinate system, 1D,2D,3D, etc }
VARIABLES        { system variables }
  V              { choose your own names }
! SELECT         { method controls }
 DEFINITIONS    { parameter definitions }
 k = 100
! INITIAL VALUES
EQUATIONS        { PDE's, one for each variable }
V: div(-k*grad(V))=0  { one possibility }
! CONSTRAINTS    { Integral constraints }
BOUNDARIES       { The domain definition }
  REGION 1       { For each material region }
    START(0,0)   { Walk the domain boundary }
    	NATURAL(V)=0 
	LINE TO (1,0)  
		NATURAL(V)=0
	LINE TO (1,1) 
		NATURAL(V)=0 
	LINE  TO (0,1)  
		NATURAL(V)=0  
	LINE TO CLOSE 

REGION 2
   START (0.2,0.6) 
    Value(V)=0
   LINE TO (0.8,0.6)  
   line TO (0.8,0.8) 
   line  TO (0.2,0.8)  
   line TO CLOSE

  REGION 3
  START (0.2,0.0) 
  Value(V)=100
  LINE TO (0.8,0.0)  
  line TO (0.8,0.4) 
  line  TO (0.2,0.4)  
  line TO CLOSE

! TIME 0 TO 1    { if time dependent }
MONITORS         { show progress }
 contour(V) as 'Potential'
PLOTS            { save result displays }
 VECTOR(k*grad(V)) as 'Electric Field'
 contour(V)
END