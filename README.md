# SYM_Lab4

###### Bourcoud Kylian, Reeve Paul, Blanc Jean-Luc

### Question 1.2

Le tremblement de la flèche est dû à une certaine imprécision des capteurs. Dès lors cela provoque des différences au niveau des valeurs retournées par les capteurs. Chaque capteur présente un niveau de précision différent et l'indique à l'aide de différentes valeurs : 

* Précision élevée : 3
* Précision moyenne : 2
* Précision basse : 1
* Précision sans contact : 0
* Précision invalide, nécessite de la calibration : -1

Plus la précision des valeurs reçues sera basse, plus la flèche tremblera dû à la différence entre les valeurs reçues.

Une solution pour résoudre ce problème serait de n'accepter que les valeurs avec une haute précision, le défaut de cette solution est que si aucun capteur n'indique de précision élevée, la flèche ne sera donc pas mise à jour. On pourrait donc imaginer n'accepter que les précisions élevées et moyennes afin d'avoir un rafraichissement de la flèche suffisamment réccurent tout en limitant les tremblements.

### Question 2.2

- Tout d'abord cela prend moins de place qu'un float (32 bits) ou un double (64 bits).
 Renvoyer un simple entier nous permet d'éviter de devoir réfléchir à une potentielle conversion du float en allant du périphérique au smartphone car la manière dont les nombres à virgule flottante sont codés dans le système du smartphone et celle dans le périphérique ne sont peut-être pas les mêmes, tandis qu'un entier est codé relativement de la même manière dans la plupart des systèmes.

- Voici la liste des caractéristiques possibles pour un service de ce type:
    - BatteryLevel: Uint8, read only + notification, Niveau de batterie entre 0% et 100%, 0 représentant une batterie complétement déchargé et 100 une batterie pleinement chargé

