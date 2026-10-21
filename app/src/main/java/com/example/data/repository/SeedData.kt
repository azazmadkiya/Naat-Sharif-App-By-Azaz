package com.example.data.repository

import com.example.data.local.NaatEntity

object SeedData {
    val INITIAL_NAATS = listOf(
        // 1. हम्द शरीफ (Hamd Sharif)
        NaatEntity(
            id = "hamd_1",
            titleHindi = "कोई तो है जो निज़ामे हस्ती चला रहा है",
            titleGujarati = "કોઈ તો છે જે નિઝામે હસ્તી ચલાવી રહ્યો છે",
            titleEnglish = "Koyi To Hai Jo Nizam E Hasti Chala Raha Hai",
            categoryId = "hamd",
            categoryName = "हम्द शरीफ",
            poet = "मुज़फ़्फ़र वारसी",
            publisher = "मक्तबा-ए-रज़ा",
            addedByName = "Azazmadkiya",
            lyricsHindi = """
कोई तो है जो निज़ामे हस्ती चला रहा है
वही ख़ुदा है, वही ख़ुदा है!

दिखाई भी जो न दे, नज़र भी जो आ रहा है
वही ख़ुदा है, वही ख़ुदा है!

नज़र भी रखे, समाअतें भी, वो जान लेता है नीयतें भी
जो ख़ाना-ए-शश-जिहत में रहकर भी बे-मकां है
वही ख़ुदा है, वही ख़ुदा है!

तलाश उस को न कर बुतों में, वो है बदलती हुई रुतों में
जो दिन को रात और रात को दिन बना रहा है
वही ख़ुदा है, वही ख़ुदा है!
            """.trimIndent(),
            lyricsGujarati = """
કોઈ તો છે જે નિઝામે હસ્તી ચલાવી રહ્યો છે
એ જ ખુદા છે, એ જ ખુદા છે!

દેખાય પણ નહીં, નજર પણ આવી રહ્યો છે
એ જ ખુદા છે, એ જ ખુદા છે!

નજર પણ રાખે, સાંભળે પણ, એ જાણી લે છે નિયતો પણ
જે આઠેય દિશામાં રહીને પણ બે-મકાન છે
એ જ ખુદા છે, એ જ ખુદા છે!

શોધ એને ન કર મૂર્તિઓમાં, એ છે બદલાતી ઋતુઓમાં
જે દિવસને રાત અને રાતને દિવસ બનાવી રહ્યો છે
એ જ ખુદા છે, એ જ ખુદા છે!
            """.trimIndent(),
            lyricsEnglish = """
Koyi to hai jo nizam-e-hasti chala raha hai
Wahi Khuda hai, wahi Khuda hai!

Dikhayi bhi jo na de, nazar bhi jo aa raha hai
Wahi Khuda hai, wahi Khuda hai!

Nazar bhi rakhe, sama'atein bhi, wo jaan leta hai niyatein bhi
Jo khana-e-shash-jihat me rehkar bhi be-makan hai
Wahi Khuda hai, wahi Khuda hai!

Talash us ko na kar buton me, wo hai badalti hui ruton me
Jo din ko raat aur raat ko din bana raha hai
Wahi Khuda hai, wahi Khuda hai!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/hamd1.mp3",
            orderIndex = 1
        ),

        // 2. नात शरीफ (Naat Sharif)
        NaatEntity(
            id = "naat_1",
            titleHindi = "वो नबियों में रहमत लक़ब पाने वाला",
            titleGujarati = "વો નબિયોં માં રહમત લકબ પામનારા",
            titleEnglish = "Wo Nabiyon Me Rahmat Laqab Paane Wala",
            categoryId = "naat",
            categoryName = "नात शरीफ",
            poet = "ख्वाजा अल्ताफ़ हुसैन हाली",
            lyricsHindi = """
वो नबियों में रहमत लक़ब पाने वाला
मुरादें ग़रीबों की बर लाने वाला!

मुसीबत में ग़ैरों के काम आने वाला
वो अपने पराये का ग़म खाने वाला!

फ़क़ीरों का मलजा, ज़ईफ़ों का मावा
यतीमों का वाली, ग़ुलामों का मौला!

ख़ताकार से दरगुज़र करने वाला
बदअंदेश के दिल में घर करने वाला!
            """.trimIndent(),
            lyricsGujarati = """
વો નબિયોં માં રહમત લકબ પામનારા
મુરાદો ગરીબો ની પૂર્ણ કરનારા!

મુસીબત માં પરાયા ના કામ આવનારા
પોતાના અને પારકા નું દુઃખ વહેંચનારા!

ફકીરો નો આશરો, કમજોરો નું ઠેકાણું
યતીમો ના વાલી, ગુલામો ના મૌલા!

ભૂલ કરનારા ને માફ કરી દેનારા
દુશ્મન ના દિલ માં પણ વસી જનારા!
            """.trimIndent(),
            lyricsEnglish = """
Wo Nabiyon me rahmat laqab paane wala
Muradein gareebon ki bar laane wala!

Museebat me gairon ke kaam aane wala
Wo apne paraaye ka gham khaane wala!

Faqeeron ka malja, za'eefon ka maawa
Yateemon ka waali, gulamon ka maula!

Khata-kaar se darguzar karne wala
Bad-andesh ke dil me ghar karne wala!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/naat1.mp3",
            orderIndex = 2
        ),

        // 2. नात शरीफ 2
        NaatEntity(
            id = "naat_2",
            titleHindi = "फ़ासलों को तकल्लुफ़ है हमसे अगर",
            titleGujarati = "ફાસલાઓને તકલ્લુફ છે હમસે અગર",
            titleEnglish = "Faaslon Ko Takalluf Hai Humse Agar",
            categoryId = "naat",
            categoryName = "नात शरीफ",
            poet = "प्रो. इकबाल अज़ीम",
            lyricsHindi = """
फ़ासलों को तकल्लुफ़ है हमसे अगर
हम भी बेबस नहीं हैं, ख़ुदारा सुनें!

उनको छूना भी आसां नहीं है मगर
रूबरू आएं तो दिल का माजरा कहें!

हाज़िरी हो मदीने में फिर एक बार
सब्ज़ गुम्बद पे नज़रें टिकी हों सदा!

उनके दर पे बिछा दें ये पलकें ज़रा
उनके क़दमों पे क़ुर्बान हो ये जहां!
            """.trimIndent(),
            lyricsGujarati = """
ફાસલાઓને તકલ્લુફ છે હમસે અગર
અમે પણ લાચાર નથી, ખુદારા સાંભળો!

એમને મળવું સરળ નથી છતાં
સામે આવીએ તો દિલની વાત કહીએ!

હાજરી થાય મદીના માં ફરી એક વાર
લીલા ગુંબદ પર નજર ટકેલી રહે સદા!

એમના દરબાર માં બિછાવી દઈએ પાંપણો
એમના કદમોં પર કુરબાન થાય આ જગત!
            """.trimIndent(),
            lyricsEnglish = """
Faaslon ko takalluf hai humse agar
Hum bhi bebas nahi hain, Khudhara sunein!

Unko chhoona bhi aasan nahi hai magar
Roobaroo aayein to dil ka maajra kahein!

Haaziri ho Madine me phir ek baar
Sabz gumbad pe nazrein tiki hon sada!

Unke dar pe bichha dein ye palkein zara
Unke qadmon pe qurban ho ye jahaan!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/naat2.mp3",
            orderIndex = 3
        ),

        // 3. मनक़बत शरीफ (Manqabat Sharif)
        NaatEntity(
            id = "manqabat_1",
            titleHindi = "दम हमदम अली अली",
            titleGujarati = "દમ હમદમ અલી અલી",
            titleEnglish = "Dam Hama Dam Ali Ali",
            categoryId = "manqabat",
            categoryName = "मनक़बत शरीफ",
            poet = "हज़रत अमीर ख़ुसरो",
            lyricsHindi = """
हैदरियाम रिंदम क़लन्दरम मस्ताना
बंदा-ए-मुर्तज़ा अली हसतम!

दम हमदम अली अली, मौला अली अली
शाहे मरदां, शेरे यज़दां, क़ूवते परवरदिगार!

ला फ़ता इल्ला अली, ला सैफ़ा इल्ला ज़ुल्फ़िक़ार
मुर्तज़ा मौला अली, मुश्किल-कुशा मौला अली!

तेरा दर छोड़ कर जाऊं कहां मौला
तू ही इमदाद कुन, तू ही मुश्किल-कुशा!
            """.trimIndent(),
            lyricsGujarati = """
હૈદરિયમ રિંદમ કલંદરી મસ્તાના
બંદા-એ-મુર્તઝા અલી છું હું!

દમ હમદમ અલી અલી, મૌલા અલી અલી
શાહે મર્દાં, શેરે યઝદાં, કુવ્વતે પરવરદિગાર!

લા ફતા ઇલ્લા અલી, લા સૈફા ઇલ્લા ઝુલ્ફિકાર
મુર્તઝા મૌલા અલી, મુશ્કિલ-કુશા મૌલા અલી!

તમારો દરબાર છોડી ક્યાં જાઉં મૌલા
તમે જ સહારો, તમે જ મુશ્કિલ-કુશા!
            """.trimIndent(),
            lyricsEnglish = """
Haidariyam rindam qalandaram mastana
Banda-e-Murtaza Ali hastam!

Dam hama dam Ali Ali, Maula Ali Ali
Shah-e-Mardan, Sher-e-Yazdan, Quwwat-e-Parwardigar!

La fata illa Ali, la saifa illa Zulfiqar
Murtaza Maula Ali, Mushkil-Kusha Maula Ali!

Tera dar chhod kar jaaun kahan Maula
Tu hi imdad-kun, tu hi Mushkil-Kusha!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/manqabat1.mp3",
            orderIndex = 4
        ),

        // 4. पंजतन पाक (Panjtan Pak)
        NaatEntity(
            id = "panjtan_1",
            titleHindi = "पंजतन के नाम पर क़ुरबान मेरी जान है",
            titleGujarati = "પંજતન ના નામ પર કુરબાન મારી જાન છે",
            titleEnglish = "Panjtan Ke Naam Par Qurban Meri Jaan Hai",
            categoryId = "panjtan",
            categoryName = "पंजतन पाक",
            poet = "परम्परागत क़लाम",
            lyricsHindi = """
पंजतन के नाम पर क़ुरबान मेरी जान है
मुस्तफ़ा, मुर्तज़ा, फ़ातिमा, हसन ओ हुसैन!

इनके दामन से वाबस्ता है ईमान मेरा
इनके सदक़े में सलामत है कुल जहां मेरा!

अहमदे मुज्तबा का ये प्यारा चमन
अहल-ए-बैते नबी पर फ़िदा तन ओ मन!

कर्बला के मुसाफ़िर को लाखों सलाम
दीने हक़ के मुहाफ़िज़ को लाखों सलाम!
            """.trimIndent(),
            lyricsGujarati = """
પંજતન ના નામ પર કુરબાન મારી જાન છે
મુસ્તફા, મુર્તઝા, ફાતિમા, હસન અને હુસૈન!

એમના પાલવ સાથે જોડાયેલો છે ઈમાન મારો
એમના સદકે સલામત છે સમગ્ર વિશ્વ મારું!

અહમદે મુજતબા નું આ વ્હાલું ઉપવન
અહલે બૈતે નબી પર સમર્પિત તન અને મન!

કરબલા ના મુસાફિર ને લાખો સલામ
દીને હકના રક્ષક ને લાખો સલામ!
            """.trimIndent(),
            lyricsEnglish = """
Panjtan ke naam par qurban meri jaan hai
Mustafa, Murtaza, Fatima, Hasan o Husain!

Inke daaman se waabasta hai imaan mera
Inke sadqe me salaamat hai kul jahaan mera!

Ahmad-e-Mujtaba ka ye pyaara chaman
Ahl-e-Bait-e-Nabi par fida tan o man!

Karbala ke musafir ko laakhon salaam
Deen-e-Haq ke muhafiz ko laakhon salaam!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/panjtan1.mp3",
            orderIndex = 5
        ),

        // 5. गौसे आज़म (Gouse Azam)
        NaatEntity(
            id = "gouse_azam_1",
            titleHindi = "गौसे आज़म बमाने बे-नवा मददे",
            titleGujarati = "ગૌસે આઝમ બમાને બે-નવા મદદે",
            titleEnglish = "Ghouse Azam Bamane Be-Nawa Madade",
            categoryId = "gouse_azam",
            categoryName = "गौसे आज़म",
            poet = "हज़रत गौसे आज़म क़ादरी",
            lyricsHindi = """
गौसे आज़म बमाने बे-नवा मददे
क़िबला-ए-दीं शहे बग़दाद मददे!

अब्दुल क़ादिर जीलानी पीराने पीर
दस्तगीरे दो जहां, रोशन ज़मीर!

तेरी चौखट पे खड़ा है ये मंगता तेरा
झोली भर दे मेरी ऐ शाहे बग़दाद!

क़ादरी रंग में रंग दे दिल को मेरे
करम का दरिया बहा दे ऐ गौसे पाक!
            """.trimIndent(),
            lyricsGujarati = """
ગૌસે આઝમ બમાને બે-નવા મદદે
કિબલા-એ-દીન શહે બગદાદ મદદે!

અબ્દુલ કાદિર જીલાની પીરાને પીર
દસ્તગીરે બે જહાં, રોશન ઝમીર!

તમારી ચોખટ પર ઉભો છે આ ભિખારી તમારો
ઝોળી ભરી દો મારી ઐ શાહે બગદાદ!

કાદિરી રંગ માં રંગી દો દિલ ને મારા
કૃપા ની નદી વહાવી દો ઐ ગૌસે પાક!
            """.trimIndent(),
            lyricsEnglish = """
Ghouse Azam bamane be-nawa madade
Qibla-e-deen shahe Baghdad madade!

Abdul Qadir Jilani Peeran-e-Peer
Dastagir-e-do jahaan, raushan zameer!

Teri chaukhat pe khada hai ye mangta tera
Jholi bhar de meri aye Shahe Baghdad!

Qadri rang me rang de dil ko mere
Karam ka dariya baha de aye Ghouse Paak!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/gouse1.mp3",
            orderIndex = 6
        ),

        // 6. गरीब नवाज (Gareeb Nawaz)
        NaatEntity(
            id = "gareeb_nawaz_1",
            titleHindi = "ख्वाजा मेरे ख्वाजा दिल में समा जा",
            titleGujarati = "ખ્વાજા મેરે ખ્વાજા દિલ મેં સમા જા",
            titleEnglish = "Khwaja Mere Khwaja Dil Me Sama Ja",
            categoryId = "gareeb_nawaz",
            categoryName = "गरीब नवाज",
            poet = "काशिफ अजमेरी",
            lyricsHindi = """
ख्वाजा मेरे ख्वाजा, दिल में समा जा
शाहे मदीना के प्यारे नवासे!

सुलतान-उल-हिन्द ख्वाजा मोईनुद्दीन
अजमेर की ज़मीन पर नूर-ए-मुबीन!

बेकसों का सहारा, ग़रीबों का आसरा
भर दो झोली हमारी ऐ ख्वाजा पिया!

तेरे दर पे झुकी है ये सारी खुदाई
हिन्द के राजा तेरी शान निराली!
            """.trimIndent(),
            lyricsGujarati = """
ખ્વાજા મેરે ખ્વાજા, દિલ મેં સમા જા
શાહે મદીના ના વ્હાલા નવાસા!

સુલતાન-ઉલ-હિન્દ ખ્વાજા મોઇનુદ્દીન
અજમેર ની ધરતી પર નૂરે મુબીન!

લાચારો નો સહારો, ગરીબો નો આશરો
ભરી દો ઝોળી અમારી ઐ ખ્વાજા પિયા!

તમારા દરબાર પર નમી છે સમગ્ર સૃષ્ટિ
હિન્દ ના રાજા તમારી શાન નિરાળી!
            """.trimIndent(),
            lyricsEnglish = """
Khwaja mere Khwaja, dil me sama ja
Shahe Madina ke pyaare nawase!

Sultan-ul-Hind Khwaja Moinuddin
Ajmer ki zameen par noor-e-mubeen!

Bekason ka sahara, gareebon ka aasra
Bhar do jholi hamari aye Khwaja Piya!

Tere dar pe jhuki hai ye saari khudayi
Hind ke Raja teri shaan niraali!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/khwaja1.mp3",
            orderIndex = 7
        ),

        // 7. कलामे रज़ा (Kalame Raza)
        NaatEntity(
            id = "kalame_raza_1",
            titleHindi = "मुस्तफ़ा जाने रहमत पे लाखों सलाम",
            titleGujarati = "મુસ્તફા જાને રહમત પે લાખો સલામ",
            titleEnglish = "Mustafa Jane Rahmat Pe Lakho Salam",
            categoryId = "kalame_raza",
            categoryName = "कलामे रज़ा",
            poet = "आला हज़रत इमाम अहमद रज़ा ख़ाँ",
            lyricsHindi = """
मुस्तफ़ा जाने रहमत पे लाखों सलाम
शम-ए-बज़मे हिदायत पे लाखों सलाम!

मेहर-ए-चर्ख़े नबुव्वत पे रौशन दुरूद
गुल-ए-बाग़े रिसालत पे लाखों सलाम!

जिस सुहानी घड़ी चमका तैबा का चांद
उस दिल-अफ़रोज़ साअत पे लाखों सलाम!

काशाना-ए-रज़ा पर हो रहमत मुदाम
सैयिदी आला हज़रत पे लाखों सलाम!
            """.trimIndent(),
            lyricsGujarati = """
મુસ્તફા જાને રહમત પે લાખો સલામ
શમ-એ-બઝમે હિદાયત પે લાખો સલામ!

મેહરે ચર્ખે નબુવ્વત પે રોશન દુરૂદ
ગુલે બાગે રિસાલત પે લાખો સલામ!

જે સુહાની ઘડીએ ચમક્યો તૈયબા નો ચાંદ
તે દિલ-અફરોઝ ઘડી પર લાખો સલામ!

કાશાન-એ-રઝા પર રહમત સદા વરસે
સૈયિદી આલા હઝરત પે લાખો સલામ!
            """.trimIndent(),
            lyricsEnglish = """
Mustafa Jane Rahmat pe lakho salam
Sham-e-bazme hidayat pe lakho salam!

Mehar-e-charkhe nabuwwat pe raushan durood
Gul-e-baage risalat pe lakho salam!

Jis suhaani ghadi chamka Taiba ka chaand
Us dil-afroz saa'at pe lakho salam!

Kaashana-e-Raza par ho rahmat mudaam
Sayyidi Aala Hazrat pe lakho salam!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/raza1.mp3",
            orderIndex = 8
        ),

        // 8. सलामी (Salami)
        NaatEntity(
            id = "salami_1",
            titleHindi = "या नबी सलाम अलैका",
            titleGujarati = "યા નબી સલામ અલૈકા",
            titleEnglish = "Ya Nabi Salam Alaika",
            categoryId = "salami",
            categoryName = "सलामी",
            poet = "इमाम बरज़नजी",
            lyricsHindi = """
या नबी सलाम अलैका
या रसूल सलाम अलैका
या हबीब सलाम अलैका
सलवातुल्लाह अलैका!

अशरक़ल बदरु अलैना
फ़ख़्तफ़त मिन्हुल बुदूरु
मिसला हुस्निक मा रअयना
क़त्तु या वज्हस सुरूरि!

अन्त शम्सुन अन्त बद्रुन
अन्त नूरुन फ़ौक़ा नूरि
अन्त इक्सीरुव व ग़ाली
अन्त मिस्बाहुस सुदूरि!
            """.trimIndent(),
            lyricsGujarati = """
યા નબી સલામ અલૈકા
યા રસૂલ સલામ અલૈકા
યા હબીબ સલામ અલૈકા
સલવાતુલ્લાહ અલૈકા!

અશરકલ બદરુ અલૈના
ફખ્તફત મિન્હુલ બુદૂરુ
મિસલા હુસ્નિક મા રઅયના
કત્તુ યા વજ્હસ સુરૂરિ!

અન્ત શમ્સુન અન્ત બદરુન
અન્ત નૂરુન ફૌકા નૂરિ
અન્ત ઇક્સીરુવ વ ગાલી
અન્ત મિસ્બાહુસ સુદૂરિ!
            """.trimIndent(),
            lyricsEnglish = """
Ya Nabi salam alaika
Ya Rasool salam alaika
Ya Habib salam alaika
Salawatullah alaika!

Ashraqal badru alaina
Fakhtafat minhul budooru
Misla husnik maa ra'aina
Qattu ya wajhas suroori!

Anta shamsun anta badrun
Anta noorun fauqa noori
Anta ikseeru wa ghaali
Anta misbaahus sudoori!
            """.trimIndent(),
            audioUrl = "https://example.com/audio/salami1.mp3",
            orderIndex = 9
        )
    )
}
