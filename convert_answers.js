const fs = require('fs');

// Your original JSON data
const originalData = {
  "answers": [
    {
      "category": "Risco de Autenticação",
      "questionId": 1,
      "selectedOption": "Exposição total de sistemas críticos."
    },
    {
      "category": "Risco de Autenticação",
      "questionId": 2,
      "selectedOption": "Acesso significativo a sistemas internos."
    },
    {
      "category": "Risco de Autenticação",
      "questionId": 3,
      "selectedOption": "Casos isolados com impacto significativo."
    },
    {
      "category": "Risco de Autenticação",
      "questionId": 4,
      "selectedOption": "Monitorização parcial e análise manual."
    },
    {
      "category": "Risco de Autenticação",
      "questionId": 5,
      "selectedOption": "MFA ativado apenas para alguns utilizadores."
    },
    {
      "category": "Risco de Autenticação",
      "questionId": 6,
      "selectedOption": "Proteção parcial das credenciais."
    },
    {
      "category": "Risco da Plataforma da Empresa",
      "questionId": 13,
      "selectedOption": "Operações parcialmente afetadas, atraso moderado."
    },
    {
      "category": "Risco da Plataforma da Empresa",
      "questionId": 14,
      "selectedOption": "Mínimo impacto reputacional."
    },
    {
      "category": "Risco da Plataforma da Empresa",
      "questionId": 15,
      "selectedOption": "Auditorias anuais ou esporádicas."
    },
    {
      "category": "Risco da Plataforma da Empresa",
      "questionId": 16,
      "selectedOption": "Sem formação ou formação muito rara."
    },
    {
      "category": "Risco da Plataforma da Empresa",
      "questionId": 17,
      "selectedOption": "Procedimentos existentes, mas pouco claros."
    },
    {
      "category": "Risco da Plataforma da Empresa",
      "questionId": 18,
      "selectedOption": "Mecanismos completos e atualizados."
    },
    {
      "category": "Risco de Armazenamento de Dados",
      "questionId": 19,
      "selectedOption": "Dados parcialmente recuperáveis, impacto moderado."
    },
    {
      "category": "Risco de Armazenamento de Dados",
      "questionId": 20,
      "selectedOption": "Impacto financeiro mínimo, sem penalizações significativas."
    },
    {
      "category": "Risco de Armazenamento de Dados",
      "questionId": 21,
      "selectedOption": "Backups automáticos e diários."
    },
    {
      "category": "Risco de Armazenamento de Dados",
      "questionId": 22,
      "selectedOption": "Atualizações regulares e gestão ativa de ameaças."
    },
    {
      "category": "Risco de Armazenamento de Dados",
      "questionId": 23,
      "selectedOption": "Criptografia forte implementada e testada."
    },
    {
      "category": "Risco de Armazenamento de Dados",
      "questionId": 24,
      "selectedOption": "Política informal ou parcialmente implementada."
    },
    {
      "category": "Risco da Rede Interna",
      "questionId": 25,
      "selectedOption": "Impacto moderado, recuperação parcial dos sistemas."
    },
    {
      "category": "Risco da Rede Interna",
      "questionId": 26,
      "selectedOption": "Exposição de alguns dados críticos, impacto moderado."
    },
    {
      "category": "Risco da Rede Interna",
      "questionId": 27,
      "selectedOption": "Monitorização parcial ou periódica."
    },
    {
      "category": "Risco da Rede Interna",
      "questionId": 28,
      "selectedOption": "Atualizações regulares e automatizadas."
    },
    {
      "category": "Risco da Rede Interna",
      "questionId": 29,
      "selectedOption": "Rede não segmentada ou mal configurada."
    },
    {
      "category": "Risco da Rede Interna",
      "questionId": 30,
      "selectedOption": "Política informal ou parcialmente aplicada."
    },
    {
      "category": "Risco de Acesso Remoto",
      "questionId": 49,
      "selectedOption": "Comprometimento parcial de serviços internos."
    },
    {
      "category": "Risco de Acesso Remoto",
      "questionId": 50,
      "selectedOption": "Impacto moderado, custos adicionais notáveis."
    },
    {
      "category": "Risco de Acesso Remoto",
      "questionId": 51,
      "selectedOption": "Não Aplicável"
    },
    {
      "category": "Risco de Acesso Remoto",
      "questionId": 52,
      "selectedOption": "Auditorias ocasionais ou não automatizadas."
    },
    {
      "category": "Risco de Acesso Remoto",
      "questionId": 53,
      "selectedOption": "Sem soluções técnicas adequadas ou insuficientes."
    },
    {
      "category": "Risco de Acesso Remoto",
      "questionId": 54,
      "selectedOption": "Não Aplicável"
    },
    {
      "category": "Risco de Configuração Inadequada de Sistemas",
      "questionId": 73,
      "selectedOption": "Impacto moderado, exploração parcial dos sistemas."
    },
    {
      "category": "Risco de Configuração Inadequada de Sistemas",
      "questionId": 74,
      "selectedOption": "Impacto moderado, danos gerenciáveis."
    },
    {
      "category": "Risco de Configuração Inadequada de Sistemas",
      "questionId": 75,
      "selectedOption": "Revisões frequentes e documentadas."
    },
    {
      "category": "Risco de Configuração Inadequada de Sistemas",
      "questionId": 76,
      "selectedOption": "Procedimentos formais e rigorosos."
    },
    {
      "category": "Risco de Configuração Inadequada de Sistemas",
      "questionId": 77,
      "selectedOption": "Uso regular e automatizado."
    },
    {
      "category": "Risco de Configuração Inadequada de Sistemas",
      "questionId": 78,
      "selectedOption": "Sem Treinos regulares ou raramente realizados."
    },
    {
      "category": "Risco de Falhas em Atualizações e Patches de Segurança",
      "questionId": 91,
      "selectedOption": "Impacto grave, comprometimento significativo dos sistemas."
    },
    {
      "category": "Risco de Falhas em Atualizações e Patches de Segurança",
      "questionId": 92,
      "selectedOption": "Impacto moderado, custos adicionais controláveis."
    },
    {
      "category": "Risco de Falhas em Atualizações e Patches de Segurança",
      "questionId": 93,
      "selectedOption": "Atualizações automáticas e imediatas."
    },
    {
      "category": "Risco de Falhas em Atualizações e Patches de Segurança",
      "questionId": 94,
      "selectedOption": "Monitorização ocasional, aplicação moderada."
    },
    {
      "category": "Risco de Falhas em Atualizações e Patches de Segurança",
      "questionId": 95,
      "selectedOption": "Ferramentas robustas e amplamente utilizadas."
    },
    {
      "category": "Risco de Falhas em Atualizações e Patches de Segurança",
      "questionId": 96,
      "selectedOption": "Processo informal, parcialmente implementado."
    }
  ]
};

// Question mapping based on the JSON files
const questionMapping = {
  1: "Qual o impacto caso credenciais comprometidas sejam utilizadas para aceder a sistemas críticos?",
  2: "Se um atacante conseguir comprometer credenciais de administrador, qual seria o impacto?",
  3: "Com que frequência os utilizadores utilizam senhas fracas ou reutilizadas?",
  4: "A organização monitoriza tentativas de login suspeitas e falhas repetidas?",
  5: "A autenticação multifator (MFA) está ativada para todas as contas críticas?",
  6: "As credenciais dos utilizadores são armazenadas de forma segura (ex: hashes robustas, vault de senhas)?",
  13: "Qual seria o impacto operacional caso a plataforma crítica mais utilizada na empresa ficasse indisponível durante um período prolongado?",
  14: "Caso uma vulnerabilidade crítica numa plataforma não seja corrigida rapidamente, quais seriam os danos potenciais à reputação da empresa?",
  15: "Com que frequência são feitas auditorias às plataformas internas para identificar vulnerabilidades ou falhas de segurança?",
  16: "As equipas responsáveis pelas plataformas são treinadas regularmente sobre práticas seguras de administração e configuração?",
  17: "Existem procedimentos claros para gestão rápida de incidentes relacionados com vulnerabilidades das plataformas críticas?",
  18: "As plataformas críticas são protegidas por mecanismos avançados de segurança, como firewalls, sistemas IDS/IPS ou proteção anti-malware?",
  19: "Qual o impacto para a empresa caso ocorra uma falha grave no armazenamento, levando à perda permanente dos dados?",
  20: "Qual seria o impacto financeiro caso ocorresse uma exposição indevida de dados confidenciais armazenados pela empresa?",
  21: "Com que frequência são realizados backups dos dados críticos armazenados pela empresa?",
  22: "As soluções de armazenamento utilizadas pela empresa são regularmente atualizadas e protegidas contra ameaças conhecidas?",
  23: "Existem mecanismos robustos de criptografia aplicados ao armazenamento de dados sensíveis?",
  24: "Existe uma política clara para gestão do ciclo de vida e descarte seguro dos dados armazenados?",
  25: "Qual o impacto operacional caso a rede interna da empresa sofra uma interrupção significativa?",
  26: "Qual seria o impacto caso ocorresse uma invasão na rede interna com comprometimento de dados confidenciais?",
  27: "A rede interna é regularmente monitorizada em tempo real para identificação de atividades suspeitas?",
  28: "Os dispositivos conectados à rede interna são regularmente atualizados e protegidos contra vulnerabilidades conhecidas?",
  29: "Existem mecanismos eficazes de segmentação da rede interna para limitar a propagação de ameaças?",
  30: "Existe uma política clara para gestão segura de acessos e autenticação dos dispositivos internos na rede?",
  49: "Qual o impacto operacional caso credenciais de acesso remoto sejam comprometidas por um atacante externo?",
  50: "Qual o impacto financeiro e reputacional se ocorrer uma invasão bem-sucedida através de canais remotos?",
  51: "O acesso remoto dos colaboradores está protegido por autenticação multifator (MFA)?",
  52: "A empresa realiza auditorias periódicas sobre acessos remotos ativos e tentativas suspeitas de conexão externa?",
  53: "Existem soluções técnicas robustas, como VPNs seguras, para proteção dos acessos remotos?",
  54: "Existe uma política formal para gestão segura dos acessos remotos (controlo, monitorização e revogação rápida)?",
  73: "Qual o impacto operacional caso um sistema crítico esteja mal configurado e seja explorado por atacantes?",
  74: "Qual o impacto financeiro e reputacional decorrente da exposição de dados sensíveis por causa de configurações inadequadas?",
  75: "Os sistemas críticos têm suas configurações regularmente revistas e validadas para segurança?",
  76: "Existem procedimentos claros para a configuração segura de novos sistemas ou serviços antes de sua implantação?",
  77: "A empresa utiliza ferramentas automatizadas para identificação rápida de configurações inseguras ou inadequadas?",
  78: "Existe treino contínuo das equipas técnicas para aplicação correta das práticas de configuração segura?",
  91: "Qual o impacto operacional caso uma vulnerabilidade crítica não seja corrigida a tempo e seja explorada por atacantes?",
  92: "Qual seria o impacto financeiro e reputacional se dados confidenciais fossem expostos devido a falhas em atualizações?",
  93: "Os sistemas recebem atualizações e patches críticos regularmente, de forma imediata e automatizada?",
  94: "A empresa monitoriza ativamente vulnerabilidades divulgadas e aplica patches relevantes rapidamente após lançamento?",
  95: "Existem ferramentas eficazes para garantir a aplicação automática e eficiente das atualizações de segurança necessárias?",
  96: "Existe um processo claro e documentado para validação, teste e aplicação rápida de patches e atualizações de segurança?"
};

// Convert the data
const convertedData = {
  answers: originalData.answers.map(answer => ({
    category: answer.category,
    questionText: questionMapping[answer.questionId],
    userResponse: answer.selectedOption,
    email: "test@example.com" // Add your email here
  }))
};

// Write the converted data to a file
fs.writeFileSync('converted_answers.json', JSON.stringify(convertedData, null, 2));

console.log('Conversion completed! Check converted_answers.json');
console.log('You can now use this JSON with the /api/answers/submit-by-text endpoint'); 