package io.clubby.server;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;
import io.stallion.services.Log;
import io.stallion.tools.SampleDataGenerator;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


public class GenerateDataAction extends SampleDataGenerator implements StallionRunAction<CommandOptionsBase> {
    @Override
    public String getActionName() {
        return "generate-data";
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void loadApp(CommandOptionsBase options) {
        AppContextLoader.loadCompletely(options);
    }

    @Override
    public void execute(CommandOptionsBase options) throws Exception {
        generate();
    }


    @Override
    public Long getBaseId() {
        return 10000L;
    }

    @Override
    public void generate() {
        createUsers();
        createChannels();
        createMessages();
    }

    private long GEORGE_ID = 10000;
    private long PAUL_ID = 10001;
    private long JOHN_ID = 10002;


    public void createUsers() {
        List<Combo> combos = list(
                new Combo()
                        .setProfile(
                                PROFILE_GEORGE
                        ).setUser(
                            new User()
                                    .setEmail("georgewashington@stallion.io")
                                    .setGivenName("George")
                                    .setEncryptionSecret("41XMF2YvTWvs")
                                    .setFamilyName("Washington")
                                    .setRole(Role.ADMIN)
                                    .setId(GEORGE_ID)

                        )
                ,
                new Combo()
                        .setProfile(
                                PROFILE_PAUL
                        ).setUser(
                            new User()
                                    .setRole(Role.MEMBER)
                                    .setEmail("paulrevere@stallion.io")
                                    .setEncryptionSecret("iO5D9pXLms0k")
                                    .setGivenName("Paul")
                                    .setFamilyName("Revere")
                                    .setId(PAUL_ID)
                ),
                new Combo()
                        .setProfile(
                                PROFILE_JOHN
                        ).setUser(
                           new User()
                                   .setRole(Role.MEMBER)
                                   .setEncryptionSecret("iO5D9pXLms0k")
                                   .setEmail("johnadams@stallion.io")
                                   .setGivenName("John")
                                   .setFamilyName("Adams")
                                   .setId(JOHN_ID)
                )
        );
        for(Combo c: combos) {
            User user = c.getUser();
            //user.setEmail(user.getEmail());
            user.setApproved(true);
            user.setEmailVerified(true);
            user.setUsername(user.getEmail().substring(0, user.getEmail().indexOf("@")));
            user.setDisplayName(user.getGivenName() + " " + user.getFamilyName());
            // password is "winfox"
            user.setBcryptedPassword("$2a$12$he7ULJLH.sh4fRlM4RTPouk0IhK.a95bbTaT/nTxAiQRqf3iPinkS");
            user.setEncryptionSecret(GeneralUtils.md5Hash(user.getEmail()));

            UserController.instance().save(c.getUser());
            c.getProfile().setUserId(user.getId());
            c.getProfile().setId(user.getId() + 200);
            UserProfileController.instance().save(c.getProfile());
            UserStateController.instance().updateState(user.getId(), UserStateType.OFFLINE);
        }
    }

    public void createChannels() {
        List<Channel> channels = list();

        channels.add(
                new Channel()
                        .setName("General")
                        .setDefaultForNewUsers(true)
                        .setChannelType(ChannelType.CHANNEL)
                        .setId(newId(500))
        );
        channels.add(
                new Channel()
                        .setName("Humor")
                        .setDefaultForNewUsers(true)
                        .setChannelType(ChannelType.CHANNEL)
                        .setId(newId(501))
        );

        channels.add(
                new Channel()
                        .setName("Officers")
                        .setChannelType(ChannelType.CHANNEL)
                        .setHidden(true)
                        .setInviteOnly(true)
                        .setEncrypted(true)
                        .setId(newId(502))
        );
        channels.add(
                new Channel()
                        .setName("Long Discussions")
                        .setDefaultForNewUsers(true)
                        .setChannelType(ChannelType.FORUM)
                        .setNewUsersSeeOldMessages(true)
                        .setId(newId(503))
        );



        channels.add(
                new Channel()
                        .setName("2b8a0a04f53760ea1852fb8f621e486a")
                        .setChannelType(ChannelType.DIRECT_MESSAGE)
                        .setUniqueHash("2b8a0a04f53760ea1852fb8f621e486a")
                        .setDirectMessageUserIds(list(GEORGE_ID, PAUL_ID))
                        .setEncrypted(true)
                        .setHidden(true)
                        .setInviteOnly(true)
                        .setId(newId(504))
        );
        channels.add(
                new Channel()
                        .setName("Plotting")
                        .setChannelType(ChannelType.CHANNEL)
                        .setEncrypted(true)
                        .setId(newId(505))
        );

        channels.add(
                new Channel()
                        .setName("Secret Plans")
                        .setChannelType(ChannelType.FORUM)
                        .setNewUsersSeeOldMessages(false)
                        .setEncrypted(true)
                        .setId(newId(506))
        );

        for(Channel channel: channels) {
            ChannelController.instance().save(channel);
        }

        Map<Long, int[]> membership = map(
                val(GEORGE_ID, new int[]{500, 501, 502, 503, 504, 505, 506}),
                val(JOHN_ID, new int[]{500, 501, 502, 503, 504, 505, 506}),
                val(PAUL_ID, new int[]{500, 501, 502, 503, 504, 505, 506})
        );
        int x = 700;
        for(Map.Entry<Long, int[]> entry: membership.entrySet()) {
            for (int channelIdBase: entry.getValue()) {
                x++;
                Long id = newId(x);
                ChannelMember cm = ChannelMemberController.instance()
                        .filter("userId", entry.getKey())
                        .filter("channelId", getId(channelIdBase))
                        .includeDeleted()
                        .first();
                if (cm == null) {
                    cm = new ChannelMember()
                            .setUserId(entry.getKey())
                            .setJoinedAt(ZonedDateTime.of(2011, 1, 1, 12, 0, 0, 0, UTC).plusDays(channelIdBase))
                            .setChannelId(getId(channelIdBase))
                            .setOwner(entry.getKey().equals(GEORGE_ID))
                            .setId(id);
                }
                cm.setDeleted(false);
                ChannelMemberController.instance().save(cm);
            }
        }

    }


    public void createMessages() {
        Integer base = 800;
        int x = 0;
        int i = 0;
        int n = 0;
        String[] froms = {"paulrevere", "georgewashington", "johnadams"};
        Long[] fromIds = {PAUL_ID, GEORGE_ID, JOHN_ID};
        for(;base<1325;base++) {
            String body = lines[i % lines.length];
            if (base > 1001) {
                body = getHundredsFormMessages().get(getHundredsFormMessages().size() - 323 + n);
                n++;
            }
            Map data = map(val("bodyMarkdown", body));

            Message message = new Message()
                    .setFromUsername(froms[base % 3])
                    .setFromUserId(fromIds[base % 3])
                    .setChannelId(10500L)
                    .setCreatedAt(ZonedDateTime.of(2016, 5, 1, 12, 0, 0, 0, UTC).plusHours(base))
                    .setMessageJson(JSON.stringify(data))
                    .setId(newId(base));
            if (base >= 820) {
                message.setThreadId(message.getId());
                message.setChannelId(getId(503));
                message.setCreatedAt(DateUtils.utcNow().minusDays((base-820) * 2));
                message.setThreadUpdatedAt(DateUtils.utcNow().minusDays((base-820) * 2));
                message.setTitle(StringUtils.capitalize(titles[(base - 820) % titles.length].toLowerCase()));
                message.setParentMessageId(0L);
                if (message.getId().equals(getId(838))) {
                    message.setCreatedAt(ZonedDateTime.of(2016, 4, 25, 12, 0, 0, 0, UTC));
                }
                if (base > 837) {
                    message.setPinned(true);
                }
                if (base > 840) {
                    message.setPinned(false);
                    message.setTitle("");
                    message.setParentMessageId(820 + (base % 20));
                    message.setThreadId(message.getParentMessageId());
                }
                if (base > 1001) {
                    message.setParentMessageId(getId(838));
                    message.setThreadId(message.getParentMessageId());
                    message.setCreatedAt(ZonedDateTime.of(2016, 5, 1, 12, 0, 0, 0, UTC).plusHours(base));

                }
            }

            MessageController.instance().save(message);
            i++;
            for (Long userId: list(GEORGE_ID, JOHN_ID, PAUL_ID)) {
                x++;
                UserMessage um = new UserMessage()
                        .setHereMentioned(false)
                        .setMentioned(false)
                        .setRead(true)
                        .setUserId(userId)
                        .setMessageId(message.getId())
                        .setDate(message.getCreatedAt())
                        .setId(newId(2000 + x));
                if (base > 834 && base < 837) {
                    um.setWatched(true);
                }
                if ((base >= 818 && base <= 820)) {
                    um.setRead(false);
                } else if (base > 820 && base % 5 == 0) {
                    um.setRead(false);
                    if (base % 15 == 0) {
                        um.setMentioned(true);
                    }
                }

                UserMessageController.instance().save(um);
            }
        }

    }

    private List<String> longThreadMessages = null;

    public List<String> getHundredsFormMessages() {
        if (longThreadMessages != null) {
            return longThreadMessages;
        }

        String html = null;
        try {
            html = IOUtils.toString(getClass().getResource("/sample-text.html"), UTF8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Pattern p = Pattern.compile("<P>[\\s\\S]*?</P>");
        Matcher m = p.matcher(html);
        List<String> paragraphs = list();
        int i = 0;
        String currentMessage = "";
        while(m.find()) {
            i++;
            String text = m.group(0);
            text = StringUtils.replace(text, "\n", " ");
            text = StringUtils.replace(text, "\r", "");
            text = StringUtils.replace(text, "<P>", "");
            text = StringUtils.replace(text, "</P>", "\n\n");
            currentMessage += text;
            if (i % 3 == 0 || i % 7 == 0) {
                paragraphs.add(currentMessage);
                currentMessage = "";
            }
        }
        Log.info("Paragraph count: {0}", paragraphs.size());
        longThreadMessages = paragraphs;
        return longThreadMessages;
    }


    String[] lines = new String[] {
            "Hare ahead under recast invidious versus more redoubtably crud like cordial the gorilla tore broke trenchant characteristically that much this before.", "Goodness owl this even moaned and horse black ireful salamander abidingly less reindeer poutingly cheered jeez this dachshund in imaginative touched much had fought lucky one hey.", "Then and ground panther badly cozy brave unheedfully much lynx some alas and until grinned hen various out misheard robustly annoying this far in yawned barring smelled one.", "That completely leered swept darn concomitant portentous less trim much more oyster one so much grizzly regardless therefore avowed much eagle saliently and chose far the directed juggled through dear far did some.", "Jeering vulture more stung hello sheep up far dizzily and smoked this one sardonically manatee so some among jeez ahead panther goodness the oh punitively expeditious much crud arousing ouch over far.", "Dear hardheaded incongruously much one after sporadic out dear ground some some far goodness astride with suspicious specially where oh conjoint more and yikes and well opposite persistently.", "Ouch below that a goodness emptied flung this amidst wildebeest extraordinary contrary set after overpaid llama when underneath wrongly more one.", "Kiwi far dalmatian dear this bastard turbulent much much ouch brusquely a due broad far less camel a contumacious woodchuck continual that retrospectively this lethargic told healthy conjoint gosh impalpable thus.", "Buffalo dear goldfish and winced far a ardent noble and one more because because pinched one coasted gull porpoise hey rakish one climbed honey and some among.", "That caught tapir but jeez tamarin whispered much far conscientiously far darn after crud adequate sparing despicably thus lemming lion crud strove more.", "That globefish jeez where shoddily said far one up hey thick a this that alas one abstrusely exorbitantly gazed imaginative cow more unblushing fiendish as goodness wow much turgidly scurrilously far climbed knelt statically.", "Since beneath some excepting less before over spontaneously grabbed bandicoot blithely handsome that caustically wrongly took ambidextrous beat and past dear less fell owl sniffed gosh much.", "Conjointly sane manful egret piteously undid snapped alas one terrier rode labrador the more manatee ouch emu less jeez in bald more inconspicuously hardy gosh underneath because hawk far.", "At lemming and boastfully boastfully hey dear so one the ravingly badger ethereally outside much more where above about alas flamingo by alas close some free disbanded.", "About melodious commendable dropped purely out hilarious tore ouch beneath truthfully far warthog crud consoled ravenous as timid creepily idiotic a that goodness cringed goodness outside tapir much more goodness.", "Hello crud above robust panther a wherever beside much ecstatic and some gorilla hey testy less terse darn more casually jeez rattlesnake jeepers the domestic much amongst that some due.", "Sociable simple in piranha hatchet excursive far that one religiously a however overdrew well yikes kangaroo stiff busily peculiar goose tautly successful.", "Far placed alas oh more and ecstatically on desolate orca crud beside goodness far aside one much and overate darn wombat artificially waved tellingly reined yet ruggedly a.", "Jeepers and forgetfully heard deliberately ruthlessly jeez peered less vigorously more less froze fumed before froze some excellent macaw and one the outbid ouch the soggy far the accidentally dear emoted however.", "Much jeez wishful far alas extraordinarily where musically more oyster due far jeez ouch overrode ungraceful wallaby below flailed truthful crucially outside much spoke cast triumphantly creatively weird."
    };

    String[] titles = new String[] {
            "THE BOOK OF THE FORBIDDEN DEMONS", "SAVING THE WORTHY BANDITS", "HER LAST SNIPER", "HOUR OF THE ARMOURED RING", "GOODBYE MR HAIRY PLANET", "DAWN OF THE SPACE SCUM", "SNAKES ON THE CRAZY RABBITS", "OUR LONELY SPIES", "THE SECRET LIFE OF ARMOURED DOGS", "DAWN OF THE SPACE MONSTERS", "THE CITY OF THE CRAZY DEVILS", "ONE DAY TO SAVE THE EXCELLENT MAN", "HARRY POTTER AND THE LONELY SPIES", "FLIGHT OF THE DANGEROUS RABBITS", "ESCAPE FROM THE NERDY ADVENTURERS", "ANOTHER ANGRY DALEK", "THE UNEXPECTED VIRTUE OF THE FAST RENEGADE", "FIND ME THE WARPED WEREWOLF", "THE LEGEND OF THE WONDERFUL WITCH", "MORE OF THE FOUL KNIGHT", "MIDNIGHT WITH THE FASCIST KNIGHT", "WHERE'S MY ALBINO GODMOTHER", "THE SECRET OF THE OLD SMUGGLERS", "I LOVE YOU LIVING DOCTOR", "THE WOMAN WITH THE BLUE GRANDMA", "RAIDERS OF THE MAGIC MUMMY", "YET MORE TEENAGE TRIPODS", "ESCAPE FROM THE FORGOTTEN MOTHERS", "THE MYSTERIOUS ISLAND OF LAZY SAILORS", "REVENGE OF THE BLUE CAT", "ATTACK OF THE HAIRY MURDERER", "CRY OF THE HAUNTED GHOUL", "THE JOURNEY TO THE UNDER-WATER ASTRONAUT", "EDUCATING THE GOLDEN CASTLE", "CRY OF THE MISSUNDERSTOOD NUNS", "YET MORE ARMOURED WARRIOR", "A NIGHT WITH A PLASTIC SAMURAI", "SEARCHING FOR THE MURDEROUS WAITRESS", "WAIT FOR THE BLOODTHIRSTY SAMURAI", "THE CITY OF THE IRON BARBARIAN", "GOODBYE TO THE DUMB DEVILS"
    };


    public class Combo {
        private User user;
        private UserProfile profile;


        public User getUser() {
            return user;
        }

        public Combo setUser(User user) {
            this.user = user;
            return this;
        }


        public UserProfile getProfile() {
            return profile;
        }

        public Combo setProfile(UserProfile profile) {
            this.profile = profile;
            return this;
        }
    }

    // key passphrase in winfoxa
    UserProfile PROFILE_JOHN = new UserProfile()
            .setAboutMe("The second president.")
            .setAvatarUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cb/John_Trumbull_-_John_Adams_-_Google_Art_Project.jpg/201px-John_Trumbull_-_John_Adams_-_Google_Art_Project.jpg")
            .setPublicKeyJwkJson("{\"alg\":\"RSA-OAEP\",\"e\":\"AQAB\",\"ext\":true,\"key_ops\":[\"encrypt\"],\"kty\":\"RSA\",\"n\":\"y_OQ4dGAHQX9c3WhOvsJxEeTqi-_mEm0SUIho2XY-K4_sG0dUb6Rml_4JoJpIn3Xp0ZvC1AsIimL_HJ31nPiJ5les5jCxnV6mkdvNoAOO1vv7AMHpMlxaGwbKrCPSrJmSm9EiBjD94TrVjKZnHLqwfFqVPWU06lYv6_-gS5U2DB5qSMOcHmeFxoQaTgye_PpWS4SdwBRJyAT7Ar4fqdipu5ej9zrFu_zGJczYIQxzNg-N1wytX46jFMSZcUngxi3oXAeGkv1agKpNWpLmWdd817tPSdCDdmIYjT7NwqDhqVGS-jAYy2DbHLZSyJMnEexLHr96HVGz__Wh0znolxuBw\"}")
            .setPrivateKeyJwkEncryptedHex("ebc2f6475e069df96f1ecfbb6d6cccc0d0939156892e36835425e5285458d1c0c419736bf54f735901740f63e917ac35494cef9d84622c277345739ababe1f5691e50c7321c1ef6384606ac1d4cdb7dc972b9d980e7752ad459f1e3d0f62c3818f05c2734429e4b25d2d80d4c6b01308f2d8eb86260f1f3066fbfdcd450448c8039121711c4fc3f45cf4e1308ad6cbee6638c94ffb3b586f4eb7034622ef2e0a962c4951e83378a0528203c1299b48baf41bbfd36d619139e6a5ebcd55272b3e9e31ddffd5400c0fc0f9aa9f5c10810b3bef20ec7c4b838ac73e9abb7cfafc62f6f815e93071f38af85f7aa8a2be1a4cc868a65c35c951fa6dd173ff8db496717ae7245b88d731afa70d5c91ccb600023a0b64e7c164e2ad289bf5deac928e57a4ae3f1143ad941ed4e1c0c5f2427453357a0a4cea6c235ad2a7b9c9031e5963a998427c613a2126692dd8c2f914baba5db309885abcdc7a11c69fa4f791240ac0c013de4780c77f920863578ab61317f2cdeb02db752a9f9bdf522c45692231b611cf2b71d7846cb1d6aeeee60e50b7434eb1e25ec69b7e4852c3edc7a82b8fd467c00b55b10c5e33b345e292653046afaaba6ea3b91cdae7574df9b35035e4eed2a36e3a82e268fe9998e3bb571339490e8351dfdce1a2524381a810eff15b04172c825f87aaf374a849a01e471bf344169dd350a9b1d2d5ef8f2e60a12113d699d17051dab73cfc51f826f3ccbdf6b3c8984a0ff44646cfb8c490f8caaeaf55509b0427123af3b8f563165140290032d3f728b1a55aa5c37f62178790ad017b559c82e78319302de6068e37f677849abb506d244d9e535a1245649f6b4249586b657ef308d4f5be7b7d899d8d2b17554e47019c03123c5e3a8ffd1f9021de28005b2adbd7df1a980581696aaf67ab1ba698cb5e4ff7e6af0c1861984dea4cdfd9ea0bbc4b985e168ad0818ca7e7410cc10b9d3a38499ff18ecb5c38c6899a484163a9a79ebbe57b811325f17265ca60b1ba6f9a2914c467b2a33febebe284f730d4ff399ea253ad96dcf5572f7ff45ee38ba3e780d76d7e8e1f37ab9c1b59ed531ca819662195ff5aeb109eb648ff248a1350cd4b9a67d3e6e6f1b237832f5f5dc05a283486b89490429c04a47722919e69398fb15a8befa764c60f2c281fa19510507e2ae00f88869f1b161a0cbb0e331ed1d8f4947854e53259fa130c961ac7c11cee86ab578faefa6e0b47af2a19004401a4067ff199ef80a0e19a7175fbbdfeda6b21acfa02d027dd460cc232655e8c34bfa412a5675aab6001ed74ad52ca6449e0f32735eb40c163339b0941e5bd28c61e1510bc234e155ef60993ef7687d72a37dad6b1b27e7c8a721e847d84c4264756d5c4be41035a2d8f7536167b9957aa4ac9914f55c274841a861cfe845b78683843547c7f69976f7ff81cd7536d15f8e78102cffe2d963feafaec1ee7608a9fa6a8bb3b3078b94d2a8759b078f4c9adde14e38b5f51235c648f71816f631db87adc1bab5d8159aa4d099012e42aa29bca58ae60be540d6dd8b2867df9ecdaadb74241a8a431f6697b7c768ec1362fac8d4a2add0153956847e1684bb25a66b6ed96d0cfdacec8aaa315721cb75bf3c68459ea9fbbb01c6cf1fc4a84270eba49c661412e2caca3924c049342d1664b4a03b3aa6b87010859c88568ef8377b193f997dde207fe7c3e79c2f7893531e5372d4012535a12b0f514addaa560037cc4ca858cd31d966ca055a8724e0c76ca6a1362e1db84bec15d160c1de672f4d39d58d5c758a331a3acefda0d5e338b9a985ea9e221c998993f69520e215046f0d4b08eb6f0a09715a973f491c89e5b0ab69dee3b1124ee4115afcfbeb75583c6a7673c6612afc965d9cad5973dbd94b38f382f4a849c6ece474e5e4bd9b07e7495e3a0eeafd9aa4d4e81c5b2f618fb227b1e629dcf0eded26549506bad1c1fc2204aeafd24a0b9fa39093562e8b543ef4aa7b552747a247fafabee65b40a3e31c354a4159449361b74a56633e065c2b57ee3eee6bd30a4304edf07c2ebfa9b2b7b76e4cc213ef6b123bd205ad9bd1ddf13ecfdef61e3433cea645daa13bae53b90e84007026f15efd9e2f325b73946400ecc863b6dd226a38bd5df20ff5c6057b0891dc97b6177de9838eafd69bbfc1dda2e88e3461e0f5ecbd74c5e2f59da16aa3ef4cb9635328577534cebcab9487db45664ab40423f5ca40e6975e8d07846f3bcd974e7c364b90ed72bb94779c47019ba1d818285acf6e314fdaa0d15ac2a557a37bb20a4971b7b02087b9ee20b9edc1cfd76280c0e429ac912a91e640cf95d67600cf122dee3241751d595c2b9d32e2566ab81d72d8cacc9f6b59454")
            .setPrivateKeyVectorHex("22968aa335972e058a82cd357184e731")
            .setPasswordSalt("200f5d2e-30ee-4ede-9748-23d96bb13f76")
            .setPasswordFourCharactersHashed("4dbd")
            ;

    // Key passphrase is "winfoxg"
    UserProfile PROFILE_GEORGE = new UserProfile()
            .setAvatarUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/f/ff/%27George_Washington%27%2C_reverse_painting_on_glass_by_William_Matthew_Prior.JPG/201px-%27George_Washington%27%2C_reverse_painting_on_glass_by_William_Matthew_Prior.JPG")
            .setAboutMe("The first prez.")
            .setWebSite("https://www.whitehouse.gov")
            .setPublicKeyJwkJson("{\"alg\":\"RSA-OAEP\",\"e\":\"AQAB\",\"ext\":true,\"key_ops\":[\"encrypt\"],\"kty\":\"RSA\",\"n\":\"xU7OrCZ2vyDntO_u5lXY5B6YmycYm9XyclMYnfKccAlyYDmw4DXvDEVMiY2UzbTDwVASQgRslxaNgBGpwG8L0XIG6GQt_FdFdPC6nver3kJ_fKes0KQUVLbmWMO66-V5MfuH-zUroYxeTbFgH8J48ZWlGx0Du1yA0wbPU-2EclZwewlZfCdN2_zVbd5zh1kD_Yz14B9HBclq00v4bAzEsVSNOe_VlNrGhtUTYXxgr28xUrrPzV5_XllxlJCff1BmeyoFI4tft-tAt6bJ7KDgsvKmHk_TnOvYbn-k_t8z2acZFadu5oiUGrWJIXhh65gUA3alpCwZqrDMQSw96aWhOw\"}")
            .setPrivateKeyJwkEncryptedHex("6b2bdba82b975fdc23029b75c908b1210a1033a58c01e15c564959fc4a14534403c268d4b0fc378b5edd2fb7429e449d13f93344ef37f643741c601b4a7b0aeb502c6ad6ccd46c319267f52037a1ceaf3038b7943dbfa63754b999ea2ae10fef486a666edc369648c2c6893c560eb995c37f60ecb3f673ada3c9df29fb3132b25282c0baa53e0073590487b9e8ed662a91a00ddd40debd40872f91844a54d72cf3aef6e9116b6ac7d20004bac4bd7a31d894ecef63bf60129ce896aa5964c9bd53e86f3c7d5c19361e175fd85f426a1792d23a290cd396ecb798b3b24761323d2e64560c2bf73bf376b68e00ddf053687000638fe2c409d79e13a9d7ae4606202b841894d0db896c154568cf1cce8b344c001a749ef1811a4f85b2d7ca6e036bb9d70b993dbb8e67983cf071cbc65a28b7f57c6c7b101ebdb07a12487abb4151911e503bdb8f6ad743e75aa648e23f58e454aa641342d98f5f952ea68d59c6dcf9ff1282d5f8443bc49b1fcbcf0eada1285a08a5bb3064caf6b51ae6ec2205143d3e4602323433ece27a1ecdbd380b57470d6f4986151d6e32dd3f2fe46a1262c4c6801428b7ca9d078b962a6afb7c9e5c9b6c49930a56e4a1aa70938a43531d04cef49f3f75a6433aa953128ce79a6938ea0839b07bb4f04f02175244d50863bc345d5143e989d70b4646c52a6e79c5c6b35f498e00cd3d57e451468c308f6845fd06f9cbc1001c225795085c9852702ef3874e9886c6b558b948d78e494d3295fda1cfe7bc961c3585ef24e8fce47f814560f7499fe9f4844c563bc5397955fcd793e91401521fa062a4e6677c0c05e2bad56b5ea15759d3f21800df94970fe948fe360d692210d6ca80e89258e2d0cfdf960d7daa44e4bb7dc697528a9b2fdfae0d58d41853b4fccc47512d019f4f06d4483ae851dc2adf9dcc345d2c9e43ee66bb420a4ace01af28a29740b7f37cf559e412fcd019f5b488d217c9a8c82c427ececc31637fb21977bba38fb4da764e2c411bf229cbe25d709c7b2496eaaca2127bf5c486aecab65e6696d66d422b5cc6f5219cdfedcf61f110afe87982cb6a7011aabc5fdc861d7fe10c34da782a3f068f7b2c03d9fc7e1aebf6878c23a7a36e21e78a3f7b8c82fdea4e85cf82df61e7286abfcdb081d8b8883be929008b73f1fab09858eaadc9aeb0ac236e57a1fec69b9210da90a7adbe7bcfdb167042cf1086d9bf02b4862c80b1b23a074a7d6e7ec5b0bc79b3a0bf07041936ee2c3e19179c097a7bbae5a8b13b03150f91c8fb49403044dc2b5969935480664803b5630d2696e440a30b3b8421b1f91765c21b399acf5fb2bb106e285c9522a6728616bca5bed66968b7140d9d6a069e4eeb840db3d4bb92ce4ec0797fc1a7f5d1ab673d624cc068031835979a703ace225afb76659c94e0ece98266061cb55c76d1812a2f0cd00ec065db87ebaea8b063f431a316b319134a0ba24bf91fdb920a6b35a2400e7504dbe39585b8926cab3a0fb52a648ac3ede0a22e68f07e00a46e4301b9e78c5d378b4b86e8aa4f2f5e54282b0c92426e37a466674d082b922221b48cddb54eef109573ded3acb3499d2edae81d04d5d3b829db41ad95067e01317dd395570bc25c0d11d0289fa891830e091dd571e427166d59421947ff7e8bbdcb57a83111dec71804dc984d78c5322657dbedf993a0da62b49d8ef321db9a12f7a4a7ca3730bdf4650909c44cffdbaeab6ee8f29f847c26e0e643f2e6f1d6aedbeb53293e06df36142f70f39088efdf6cb4c741fe15a20d10009d2705ef197d3232f76bc2cb3ad9d543c7e1dc6665530eb961c4773df2cfd0cdb877618768c8c3d5a1b4e7c7027f29f094d1deef386d5014bcf52a7506a2814a1ece29e6d1e024ccabc0e54d8ca474f0070a67a4f9c9d128befb8af6e2430277d5ba7a54b1fdec3356f83dfb5b8ea640aa868ebc1964a8e299e266cc5aeaf9e6e73327042bd6d0b6d70f29f1af4a8b0e7c4ca026c880ca91554b6b951ad2fa64549855411696cc5445d1aef67217edb64a7750248d48dd1fe42afc06515e53f56a77568bd916bf558c28d4b84fab08dae5435661370fa2f669dab4646cfefc80fec3d1a41b68e60977dc3eec0143657ea5aa747d30237ad02d535605b7f968dd5b2396bfb2f668b6ddbb93a649d3db0e57a9e06d3c61f511882c9477e7ddf8e8caf0fd4e1b1dd1b2cb82433f137e0e34aaaea5dba547cf6361183438216126ec4e33f7f3ae83cc85d0d1c935938b8a076c91999c816c2a7e8acfbdbb8cab4c621fced9f8070d395c3a9e0c55bd549c10028c8d40aadc40b8006accc6162c9f4522725adc561ad44ba99d3e8483ab949db304bf3a3b2e6da5")
            .setPrivateKeyVectorHex("f4b1a8dbbe9c677e2d97a671f8259b49")
            .setPasswordSalt("f66abe7d-1f24-47ec-981c-59e11bb54ad0")
            .setPasswordFourCharactersHashed("9058")
            ;

    // Key passphrase is winfoxp
    UserProfile PROFILE_PAUL = new UserProfile()
            .setAboutMe("Made a midnight ride")
            .setAvatarUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/e/eb/Paul_Revere.jpg/220px-Paul_Revere.jpg")
            .setPublicKeyJwkJson("{\"alg\":\"RSA-OAEP\",\"e\":\"AQAB\",\"ext\":true,\"key_ops\":[\"encrypt\"],\"kty\":\"RSA\",\"n\":\"1l33NQrI7ff4bbLF-mupNibBa4IFZMaKFzpXhdUgw-Z77zPWaJutzp_EuO_lYRVf0a6hxleJpEg-r1l6LcHNfUoarfeSKu0FeyErmo9-3JSa-HnpNAA0Hv_ztuwU-odktr60aqX1KRsEnvhCj44tt3C70auMfc5c1kF1oWvbMZMCsGhT7b-NxkylR1DfhR0bJ5silFbFobD-JOoFPizKVetRHVy8OX6uR3-IU-xVeLPYT3gYNNbnIidMq1b7cbwpE2jS8MjAwYOoIRpVmOi6Mj-nrefT8OlTmesV-Soj7O7izpb2SPUr9ktOx-12k9QUqI_ybSp0vnhSgA2qBeGDmQ\"}")
            .setPrivateKeyJwkEncryptedHex("b3ff6c2788773fb0f1e1da3c858b9ff33e2eba6441e5304a14a2af98896bb04706ec167f55a95432004cb7bc6516d61c5ca512c9a471a5333d2b163ec9683702c87e71fd552fb8d7213e50a63d6b2ebd4f2fb46ac653630f64ae223599cb695a5c3fc7781c9f59928166ce6810654456bae8ca6f11cd29f8064ee63dd789196a2ec461f6e0dd11b21f1598b3b9bf2669619fc60c7d2c05627dd944075f2061be2d8a3334bec868537877ba06edc9139ec9827f483f30372a65fb52b3915ff141803959c308107bec257fddddbf1f8a5a2425350e946d7748aa9c834f6f25bd6b80a082fbf61f3cf41d47b4692087830c4837fdc166e39cf13a7334b2dc7030aa772eecd92ae39175e9401aa86a28b7b960229f168939117a2ef4b0ccf821d0331ba968db0ddd8c9bbdddc49bc3d6109b7fd158a828766fa9c71ab2c25e6d1af49f7d9b76f5f45e25da070c70211b53ccbb1be777c7b4cc91ea8d8f79daed496e83055e916253d6fd081b48cb03dc722837e54ac1f5b745f1ab982aca0f905d56d730204fda5a2e6d1a89ddb5095019e3e0740501dcff26149e97265d70df400dc92f8257a929fcbc3fe050ad15bf9e6734bb20206cd795043b228ab40708f548bbd5f1679185319ff1ba6c545e05e70e249f2ee63bbe413886da5201f6841ceaa9be8e4264e6125a8c8fe5ba687299c7006cf75ef9ee03dd9936d4b7d4a84d207f6a90883fac85b4aac79eb98c39d8f9aa918b081ca55d304d2eeb33030e361c9aed46b63a35ddb746a54a6f348fe539565278ce2ae0be55aee2d9bda64f1468d257474c7c5de349eb2fe371cc4950087f60a1d9dd43e7a2d6a5dba689f4f311a07f96b9437ba518b34abe6a1f155e41abf24f9282a82efeade00cbef7a0ff2b6323d11a7592235a1fa40837626f8179622600d8f11d76db35b5c425c8eebe244a708735089ca84172fe3693b4dafd6cfd6b1c262e5ad74e4873dbd7ff25c3c24720e4970607316463854c4f45c370408b437c8220e71e48577c2d7477cf1e19dc8c772225c7bc66419e39e59cc73a36ba43ffdb92f3a7b7ba3f1f5b2f47264c7cfe6a8faacf995d38cef3c24fa38e4407299627814fdfdc8b9669c3079ec1c32955b5ee4e6ce61ad82e948d232f43b01405dfbe249f43600bc446709ca323a265d730b91ec0fcecda852ecb369a5b8431441c404a2b0324f023c317d3f14d5d2514b75e8e6905e02ab3418e9a4b092523f3c873d5eca247f2cd708e9aa5d0dbc3c87afd4b2595ad86897279419bc75d9c7e876ae800f23a9c0ced561cbd23e2d4eee531061424f659696438382d26efd41b9178764be748270c423ea8fd209a23680b1f29456cbb1dcb7d9a2d88505b7e4cd797a6d48176400e38ce80136cec903fb6c4f96b59b9987b2837deb6e028a7668fe8cb71622437d024260735799de5acf1ef1910c75ff08dc6342a8328e2f4f85021b2b13c9e6e745b33ac4154b7c2931c97878f735720204f0cd41877f277d8e341c05fef43671089bc921b068e2f026892d57f9787f5d336bb869c2ec4048af0824204f58cbe5123ec40af0dab81029711def31ab4415566ceced4235fc89cb21807039f4d0b3339b5769bf384ce29dd01ccc2f06f1774a3a9b22a5dd8b75eabe28948a96b2e2e8106f2277240c848ac5694e8e7c1b786443fd8dc9517b1a459cd774bad02d9e8d1fb34585fd2719cdf0ecb82a876aeda5b4eddc171e6b9434c60be98eb95dbc847d107c709f4c81f3764f8655a35ec892727968a19b4f15df531494c488f5dd7ba3e02f8f0624644e0f9221b34479b3066323560c1b3a64041207b0ab0438b6fbc59db914650fb4d5c721e3badeee43e0941430aa533f916f9275dae8542d20c87fc47a61eeeddb6623152d6c251617a7b0fbcef26d30cdbb96765a7e8988dd0bed9bac6e14284d6b76df138067057b9c0058b6a47b0c74a1ee5f3d0cc7cc804270b82b8f6079eefeb366cf6eba0a206842da6e667b462f467a007a71533f21d39f3555eb9fe22a9577d937ca835c5ceb8b705589a1ae5000b26dcaef074fd8ec192fb937ab96a127a89d1e862c4c8cbd9fa9838d9a6f45e9d7970ad4f71266a7a223f14aeb2b756ec8d4bca8c53b0eff183b5d0c2a58f2fa1ab7d327ee58c6615bb771aec148e3a5d215b952d8eb67f91f2205449ebab404af39831215f6dc1c6ae01f280dfe01a8db60adb1c97db295f1147b0910007aedb449e68431a6153cd819e3050b62b39673173c596fd5d6f67df05028290bb543a946a160ea7a4d47a72278dd68af2475044f50fd117704964299423972bf41c356adb7e13d5834b3d888b1877252f299e30adfb048b0aef0db8a627740846")
            .setPrivateKeyVectorHex("2dad34e6abb34bf6eee39855a209c888")
            .setPasswordSalt("616f3875-633d-45f5-9749-ba7e7d714c6f")
            .setPasswordFourCharactersHashed("d8d8")
            ;



}
