// Currying
Function busca_tabela(String nome) {
  return (List<String> camposList) {
    return (dynamic condicao) {
      Map<String, dynamic> query = {
        "tabela": nome,
        "campos": camposList,
        "where": condicao
      };
      return query;
    };
  };
}

// Função pura
String comparadores(Map<String, dynamic> filtro) {
  String campo = filtro["campo"] as String;

  if (filtro.containsKey("igual_a")) {
    return "$campo = ${filtro["igual_a"]}";
  }

  if (filtro.containsKey("maior_que")) {
    return "$campo > ${filtro["maior_que"]}";
  }

  if (filtro.containsKey("menor_que")) {
    return "$campo < ${filtro["menor_que"]}";
  }

  if (filtro.containsKey("maior_igual_que")) {
    return "$campo >= ${filtro["maior_igual_que"]}";
  }

  if (filtro.containsKey("menor_igual_que")) {
    return "$campo <= ${filtro["menor_igual_que"]}";
  }
  
  if (filtro.containsKey("em")) {
    return "$campo IN ${filtro["em"]}";
  }

  return "";
}




// E
String e_s(List<dynamic> condicoes) {
  var sqls = condicoes.map((c) {
    if (c is Map<String, dynamic>) {
      return comparadores(c);
    } else if (c is String) {
      return c;
    }
    return "";
  }).toList();

  sqls = sqls.where((s) => s.isNotEmpty).toList();

  if (sqls.isEmpty) {
    return "";
  }

  return sqls.reduce((acumulado, atual) {
    return "$acumulado AND $atual";
  });
}

// OU
String ou_s(List<dynamic> condicoes) {
  var sqls = condicoes.map((c) {
    if (c is Map<String, dynamic>) {
      return comparadores(c);
    } else if (c is String) {
      return c;
    }
    return "";
  }).toList();

  sqls = sqls.where((s) => s.isNotEmpty).toList();

  if (sqls.isEmpty) {
    return "";
  }

  String resultado = sqls.reduce((acumulado, atual) {
    return "$acumulado OR $atual";
  });

  return "($resultado)";
}




String aQuery(String campo, String tabela){
  return "SELECT $campo FROM $tabela";
}

String pegarTodosItens(String tabela){
  return aQuery("*", tabela);
}

String juntarQuery(Map<String, dynamic> query) {
  List<String> campos = query["campos"] as List<String>;
  String campoValor = "";
  String sql = "";
  
  if (campos.isEmpty) {
    sql = pegarTodosItens(query["tabela"]);
  } else {
    campoValor = campos.reduce((acumulado, atual) {
      return "$acumulado , $atual";
    });
    sql = aQuery(campoValor,query["tabela"]);
  }


  
  if (query["where"] != null) {
    var condicao = query["where"];
    String condicoes;

    if (condicao is Map<String, dynamic>) {
      condicoes = comparadores(condicao);
    } else if (condicao is String) {
      condicoes = condicao;
    } else {
      condicoes = "";
    }

    if (condicoes.isNotEmpty) {
      sql += " WHERE $condicoes";
    }
  }

  return sql;
}

void main() {
  var buscaUsuario = busca_tabela("usuario");
  buscaUsuario = buscaUsuario(<String>["nome","email"]);
  var queryFInal = buscaUsuario(e_s([
    {"campo": "nome", "igual_a": "Giovanna"},
    {"campo": "idade", "maior_igual_que": 20},
    {"campo": 'id', "em":  [10, 20, 30]},
    (ou_s([
      {"campo": "matriculada", "igual_a": true},
      {"campo": "status", "igual_a": "quase_lá"}
    ]))
  ]));

  print(juntarQuery(queryFInal));
}