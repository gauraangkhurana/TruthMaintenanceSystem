# TruthMaintenanceSystem


This is a justification based truth maintenance system.

You can input various truth statements in the format:-

        Tell: A>B
        Tell: C>D
        Tell: A
        Tell: (C*B)>E
        Tell: (E+D)>F
        Tell: C
        Tell: A>-F
        Retract: C
    
The program will maintain and print the inference derived from the statements. This is known as a Truth Maintenance System.
It will also provide justification of the derived inference, therefore it is of type - justification based system.
    
The output format will specify inference and also the reason behind the inference in the format.

    Here D is derived as we know C and D can be infered from C. Similarly, E, F and B are also infered. On the contrary, if we add -F when 
    F is already derived then the system will give a conflict error as shown below.
    ------------------------------------------------------------
    A>B
    C>D
    A
    B: {A, A>B}
    (C*B)>E
    (E+D)>F
    C
    D: {C, C>D}
    E: {C, B, (C*B)>E}
    F: {E, (E+D)>F}
    F: {D, (E+D)>F}
    ------------------------------------------------------------
    Conflict :	A>-F
    ------------------------------------------------------------
   
