#lang racket

(current-directory)
(define fp (build-path "C:" "work" "scheme"))
(current-directory fp)
(current-directory)


; (define in (open-input-file "3by3_inputdata.txt"))
; (close-input-port in)
; (file->lines "3by3_inputdata.txt")


(define (readTableau fileIn)  
  (let ((sL (map (lambda s (string-split (car s))) (file->lines fileIn))))
    (map (lambda (L)
           (map (lambda (s)
                  (if (eqv? (string->number s) #f)
                      s
                      (string->number s))) L)) sL)))
    
;(readTableau "3by3_inputdata.txt")

;(define tb (readTableau "3by3_inputdata.txt"))

(define (writeTableau tb fileOut)
  (if (eqv? tb '())
      #t
      (begin (display-lines-to-file (car tb) fileOut #:separator #\space #:exists 'append)
             (display-to-file #\newline fileOut #:exists 'append)
             (writeTableau (cdr tb) fileOut))))
                             
; (display-lines-to-file (readTableau "3by3_inputdata.txt") "test.txt")

; (writeTableau tb "test.txt")



; Convert the given tableau to vector
(define (list->vec l)
  (cond
    ((null? l) '#())
    (else (let ((vec (list->vector (car l))))
            (vector-append (list->vector (cons vec '())) (list->vec (cdr l)))
           )
    )
  )
)

; Convert the given vector back to lists
(define (vec->list vec)
  (let ((l '()) (len (vector-length vec)))
    (do ((i (- len 1) (- i 1)))
        ((= i -1))
      (set! l (cons (vector->list (vector-ref vec i)) l))
    )
    l
  )
)


; Sets the value of matrix i j to v
(define (setMatrix mat i j v)
  (vector-set! (vector-ref mat i) j v)
)

; Gets value of matrix i j
(define (getMatrix mat i j)
  (vector-ref (vector-ref mat i) j)
)

; Gets the current demand
(define (getDemand costs j)
  (vector-ref (vector-ref costs (- (vector-length costs) 1)) j)
)

; Gets the current supply
(define (getSupply costs i)
  (vector-ref (vector-ref costs i) (- (vector-length (vector-ref costs 0)) 1))
)

; Sets the value of the demand
(define (setDemand costs j demand)
  (vector-set! (vector-ref costs (- (vector-length costs) 1)) j demand)
)

; Sets the value of supply
(define (setSupply costs i supply)
  (vector-set! (vector-ref costs i) (- (vector-length (vector-ref costs 0)) 1) supply)
)

; Send supply on route
(define (sendSupply costs transported i j amount)
  (setMatrix transported i j (+ (getMatrix transported i j) amount))
  (setDemand costs j (- (getDemand costs j) amount))
  (setSupply costs i (- (getSupply costs i) amount))
)


; Create empty matrix for initial solution
(define (initialMatrix l)
  (let ( (row (- (vector-length l) 1)) (col (- (vector-length (vector-ref l 0)) 1)) )
    (do ((i 1 (+ i 1)))
        ((= i row))
       (do ((j 1 (+ j 1)))
           ((= j col))
           (setMatrix l i j 0)
       )
    )
    l
  )
)

; Find the position '(cost i j) of the smallest cost that is not full and can be transported supplies
; returns false if nothing found
(define (cheapestCell costs transported)
  (let ( (row (- (vector-length costs) 1)) (col (- (vector-length (vector-ref costs 0)) 1)) )
    (let ( (currentMin (list -1 -1 -1)) )
      (do ((i 1 (+ i 1)))
          ((= i row))
          (do ((j 1 (+ j 1)))
              ((= j col))
            (cond
              ( (and (> (getDemand costs j) 0) (> (getSupply costs i) 0) )
                (cond
                  ((eqv? -1 (car currentMin)) (set! currentMin (list (getMatrix costs i j) i j)))
                  ((> (car currentMin) (getMatrix costs i j)) (set! currentMin (list (getMatrix costs i j) i j)))
                )
              )
            )
          )
      )
      (if (eqv? -1 (car currentMin))
          #f
          currentMin
      )
    )
  )
)

; Finds the initial solution by iterating over the cheapest cells
(define (initialSolution costs transported)
  (do ((minCell (cheapestCell costs transported) (cheapestCell costs transported)))
      ((equal? #f minCell))
    (let* ((i (cadr minCell)) (j (caddr minCell)) (amount (min (getDemand costs j) (getSupply costs i))))
      (sendSupply costs transported i j amount)
    )
  )
  transported
)

; Takes input file and finds the initial solution and writes to outputfile
(define (minimumCell inputFile outputFile)
  (let* ((inputMatrix (readTableau inputFile))
         (costs (list->vec inputMatrix))
         (transported (initialMatrix (list->vec inputMatrix))))
    (writeTableau (vec->list (initialSolution costs transported)) outputFile)
  )
)







