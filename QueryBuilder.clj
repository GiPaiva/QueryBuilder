;; ============================================
;; Funções auxiliares
;; ============================================

; Função pura
(defn formatar-valor [valor]
  (if (string? valor)
    (str "\"" valor "\"")
    (if (number? valor)
      (str valor)
      (if (boolean? valor)
        (str valor)
        (str "'" (name valor) "'")))))


; Função pura
(defn formatar-lista [valores]
  (str "(" 
       (reduce (fn [acc v] 
                 (if (= acc "")
                   (formatar-valor v)
                   (str acc ", " (formatar-valor v))))
               "" 
               valores)
       ")"))


;; ============================================
;; Comparadores
;; ============================================

;Função de Currying
; Uma função que retorna outra função
; e que utiliza o parametro da primeira chamada
(defn compara [campo]
  (fn [tipo valor]
    (if (= tipo :igual_a)
      (str campo " = " (formatar-valor valor))
      (if (= tipo :diferente_de)
        (str campo " != " (formatar-valor valor))
        (if (= tipo :maior_que)
          (str campo " > " (formatar-valor valor))
          (if (= tipo :menor_que)
            (str campo " < " (formatar-valor valor))
            (if (= tipo :maior_ou_igual)
              (str campo " >= " (formatar-valor valor))
              (if (= tipo :menor_ou_igual)
                (str campo " <= " (formatar-valor valor))
                (if (= tipo :em)
                  (str campo " IN " (formatar-lista valor))
                  (if (= tipo :nao_em)
                    (str campo " NOT IN " (formatar-lista valor))
                    (if (= tipo :like)
                      (str campo " LIKE " (formatar-valor valor))
                      (if (= tipo :valor)
                        (str campo " = " (formatar-valor valor))
                        "ERRO"))))))))))))

;Função de alta ordem (porque chama uma função e usa o retorno da mesma)
(defn comparador [comp]
  (if (map? comp)
    (let [q1 (compara (name (:campo comp)))]
      (cond
        (:igual_a comp)         (q1 :igual_a (:igual_a comp))
        (:diferente_de comp)    (q1 :diferente_de (:diferente_de comp))
        (:maior_que comp)       (q1 :maior_que (:maior_que comp))
        (:menor_que comp)       (q1 :menor_que (:menor_que comp))
        (:maior_ou_igual comp)  (q1 :maior_ou_igual (:maior_ou_igual comp))
        (:menor_ou_igual comp)  (q1 :menor_ou_igual (:menor_ou_igual comp))
        (:em comp)              (q1 :em (:em comp))
        (:nao_em comp)          (q1 :nao_em (:nao_em comp))
        (:like comp)            (q1 :like (:like comp))
        (:valor comp)           (q1 :valor (:valor comp))
        :else nil))   
    comp))

;; ============================================
;; Funções para combinar condições
;; ============================================


;; Função alta ordem e currying
(defn combinador [separador]
  (fn [condicoes]
    (reduce (fn [acc parte]
              (if (= acc "")
                (comparador parte)
                (str acc separador (comparador parte))))
            ""
            condicoes)))

;; Funções especializadas 
;(parciais apenas no sentido de especificação)
(def combinar-com-and (combinador " AND "))
(def combinar-com-or (combinador " OR "))

;; Funções de alta ordem 
(defn e_s [condicoes]
  (combinar-com-and condicoes))

(defn ou_s [condicoes]
  (str "(" (combinar-com-or condicoes) ")"))

;; ============================================
;; Construindo campos
;; ============================================

; Função Pura
(defn juntar-campos [campos-lista]
    (if (nil? campos-lista)
      "*"
      (reduce (fn [acc campo]
                (if (= acc "")
                  campo
                  (str acc ", " campo)))
              ""
              campos-lista)))

;; ============================================
;; Gerando Query
;; ============================================

;Função currying
(defn criar-query [tabela]
  (fn [campos-lista]
    (fn [where]
      (str "SELECT "
           (juntar-campos campos-lista)
           " FROM " tabela
           (if where
             (str " WHERE " where)
             "")))))


;; ============================================
;; Entrada
;; ============================================

(def q1 (criar-query "usuario"))
(def q1-campos (q1 ["nome" "qi"]))
(def q1-final (q1-campos
                       (e_s [{:campo :nome :igual_a "Giovanna"}
                             {:campo :idade :maior_ou_igual 20}
                             (ou_s [{:campo :matriculada :igual_a true}
                                    {:campo :status :igual_a "quase_la"}
                                    ]
                              )
                            ]
                        )
              )
)
(println q1-final)

(def q1 (criar-query "usuario"))
(def q1-campos (q1 ["nome" "qi"]))
(def q1-final (q1-campos nil))
(println q1-final)