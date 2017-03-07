package io.stallion.clubhouse;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.boot.AppContextLoader;
import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.StallionRunAction;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.tools.SampleDataGenerator;
import io.stallion.users.Role;
import io.stallion.users.User;
import io.stallion.users.UserController;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;


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
                        .setChannelType(ChannelType.CHANNEL)
                        .setId(newId(500))
        );
        channels.add(
                new Channel()
                        .setName("Humor")
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

        for(Channel channel: channels) {
            ChannelController.instance().save(channel);
        }

        Map<Long, int[]> membership = map(
                val(GEORGE_ID, new int[]{500, 501, 502, 503, 504, 505}),
                val(JOHN_ID, new int[]{500, 501, 502, 503, 504, 505}),
                val(PAUL_ID, new int[]{500, 501, 502, 503, 504, 505})
        );
        int x = 700;
        for(Map.Entry<Long, int[]> entry: membership.entrySet()) {
            for (int channelIdBase: entry.getValue()) {
                x++;
                Long id = newId(x);
                ChannelMember cm = new ChannelMember()
                        .setUserId(entry.getKey())
                        .setJoinedAt(ZonedDateTime.of(2011, 1, 1, 12, 0, 0, 0, UTC).plusDays(channelIdBase))
                        .setChannelId(getId(channelIdBase))
                        .setOwner(entry.getKey().equals(GEORGE_ID))
                        .setId(id);
                ChannelMemberController.instance().save(cm);
            }
        }

    }

    public void createMessages() {
        Integer base = 800;
        int x = 0;
        int i = 0;
        String[] froms = {"paulrevere", "georgewashington", "johnadams"};
        Long[] fromIds = {PAUL_ID, GEORGE_ID, JOHN_ID};
        for(;base<880;base++) {
            String body = lines[i % lines.length];
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
                message.setParentMessageId(0);
                if (base > 837) {
                    message.setPinned(true);
                }
                if (base > 840) {
                    message.setPinned(false);
                    message.setTitle("");
                    message.setParentMessageId(820 + (base % 20));
                    message.setThreadId(message.getParentMessageId());
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
            .setEncryptedPrivateKeyInitializationVectorHex("436e7ef5ef5868ba29b88ba3a75b34ee")
            .setPublicKeyHex("30820122300d06092a864886f70d01010105000382010f003082010a0282010100b70f214a0961f8bf0a128ad4db0781e3fa121b5e6cbd8132b048c1a138cb7489d07324894dc65e2e9afa4de1b62391203568a002b8c533f9b10dcbff9e5d5c8400e9fbb082f395a39af947fef3e57605c82598f53d947c1a901ff3088ed912c587dced45cb8ac346495bc5d4a815761f70e4056b9108aa6b0924ab0e9a7fe6bca03099a6baf68612fc55c333490f5c776438f2be83a9de155bc347c119c0404bb9cf68e555e8db703c9b7cd4f49fcce252aa8ffffdf51305da53e4176875bf6d4f69e380d3824465599b3de2d25888d724596418f1d3b5ced023246c931b3203068511fc81ed0e3f895c233eb1981337bb3033c783021293bac442ab40e3c7dd0203010001")
            .setEncryptedPrivateKeyHex("5b80ce734397c43b1d6f221811e827b37c173284c8216f67b5f25c00574367ff8bac955c6a7259cf39bd1273d030bb83f894fb9d9d8198bac0a18676f4c820b3ec310e41beb5666278d7949dabe01f4e254c07fa797b68024f480374ffb6a516f0370f5143ee509cccf805b312b2cbca712b7eeb8b70b33ec76f522c2649527b3293dee8bf59ae2188702ca35468f42db4b006690f7915532d851ece75fd067dfbe1681b4f9319f8bffc2a7c9a74bec84fa6a26572307396dd9c31702c42b2deac87c151dc0c99517593d9a7acfeb445b03960009d57f5782457ecbe419aea234744e7961b4b6f75505008db2525dbd2866fba9f6f84cd1aca44ea23a090fc784dfbf5a76c8041c25f4ab40d224e78aa4ae40ca7d17dc81ab975216afd4ef2ac61bf1efe47c6e0edacfa7fbb988d394784cdccd3af84ce8f7fcde7ac339e4ad037ce60e0ae3e0d5a8ad477ba6a0c9525ed93429356fbde233ef9b99fba9bc25c1774f7d0d74b6c5bd9385ebf0011cf81e032ec3a2a9363fe4044ac2dc9870a970fab9a4984d8bb426966ed72675d73bbfdd42f462f6889df0dac46b52c020e50e3627369df47d5072587deb40d7d984ba0879e7f63e6fd48f6060c93254312b82c49723931c9c0acc65843ebcc8c70206157fb82b923ad97889784e6f36bf62f472d16152cc01d629953a70d729e920be7536cc8d6ed3ea8ac44441640ef32b2635e4f81c4f131ec0197cd72e31908562fa06f23e321c66252e4d954eafe2e463a9013ec11e5293102a37d10cfa3794137bbf8a331637a8d3c89289a2c0876e4f216d173d4e10b36985ac6773332e640b34f81b76f841954725621ca15b6959b9e96d5c79aa0c11b5e2aae8e3bdd349809a9afb1d9c544a012d79439d090097c2d7e7f335f9d0259ad02567787d744a0a6c17aae4cc07e9409b488b1273f46cb763452997e4c352953756bcc0780b852b87955dfe8082513d25ea2440ddde5dca7dbb44efd78718ce802ac973da91cc59d5db989c4ad718c76eeaf8ca057c79fc003f1cd59f9105d47817594d9e18737d6d63b3cb1af018b5cd36f96477492c962b380965aefbf54b6c1faa1a3fbb643cb04020d3b6b8b88d607d66eecfa8475255521fae1201dc581ede822f944e9fed8b25765b52de613027d23087d9981c653c606511fb75ff95c8a2851085933b2996ad877ae60c3eaa8120252eba2a94ea16502dbb8667a4e05eb795241c07b4ac59c0cb3ab2911c5234eaa540d2b7e7f7fe39d55d0aec6598f979cb09dc970a886a59539417bc2341e147b451deccec86254b70f99a77852689e016e261f9d9f0cde0a84a1145728a9a5998148fdf77bd370dbd2e493d58e3bb26c29d8ec2578e1172adb0cc1d009ab8e9c782600bbbfe6bba6a158c54c66ad97e5e84ce9f16daad0084f0e37fcfd1ded63eefaed8892f6e6e4b8ceaea8f3ef57b61db735a855f3bcfc5e1e4043089e2cd60b5cd722220543b483d82d21ee591551ad314fad95dde75c37337a96fef36096a2eb2adf151f3254499451f1917a0ac79dfae3893c7317943603e24f2444239928e47f3e03fa937cbddd8061ce75abaa8711fe84725a4e9b333fbbfae68ff56dda229629736afb1e0fa37dcbabfc8177fa2b9ae445fed8e14828916771b17144a5336d279e376766c46a3bdf27cef83cd1e9626d7fa0fe9919f964c5c733f1c8026ce4032a58e12018511d9b7b469229b15fb4ff94")
            ;

    // Key passphrase is "winfox"
    UserProfile PROFILE_GEORGE = new UserProfile()
            .setAboutMe("The first prez.")
            .setWebSite("https://www.whitehouse.gov")
            .setEncryptedPrivateKeyInitializationVectorHex("62c53b1dfd2f8ea1555f78d4cdfdeccc")
            .setPublicKeyHex("30820122300d06092a864886f70d01010105000382010f003082010a02820101009e908b044b5e215b8718caec3622e32999901ca3a1b0283fc0eb11822b5bf62efc2286686f704bd0a2d5274d69067a75243de471a18dc21c24a6a9586709402b0cd9b930952de722a302f444aaa23239dacacb8de433300845de3c018c246f8ad85cc07eff04ad0236e010d68597972e5118515cfa0e2f6d27154a160f2b8fdfedf63faf0ef00141653aa479418ecedb4ab96689a2ebdad05e43af210b513115b1ccf8735240d859c9911acbcc4dd01d26335ab19c87c50be5eab120ffcf99a38e0209d2fc534ca6b3216d1e6e96ffe51e6d0015a6e73d22d5a72cc7e74fcfe50752a3601fad59f058737d31fe2da870793a4b925058ab238a7b25ccb299a7f70203010001")
            .setEncryptedPrivateKeyHex("d763d3470248b2b347fe8d62598212452fa3a2b3a25eb54a345cc2857668908c47316feb2c105df71becb64ec32f56abed66b9b6e54a686ec18f0c2fa43ac1bef1a65716c01ae67ac4435d11331432a5078919e964bfed4bfadf55392fc5d8c176f1d6d7d9413e9c746ee96fc093b445ef0769a35313556d21312788f146e93f1c06008b4c7f8dc502d268d56fc491862fdb44d95abdd7e552bff3106d49cbd48bdc917f56c9df1c0c5808530ee6f1da24512e4279585fd2d21e6703e14c34e471cb5547a192f016e39f35500517d606259ae8bf81f12b8813d9e89bd0846a14709759bf8a3ea8a700812366cf4ab0477861e4afef44564cf1b1fae3e6b70794293f8d1c7da3ed98c1e1e9c3743da6d615e12df0190bbd60878f4362b690d8c0b3735e5d888c541e291c06f38d360c919e0282b2073185af222e0a38decdec76150152c881628371cec5f09a7d563e779819ad9c22d1a3d945f6c45ac58705d84f8c18cb42e095e282a0539b50a6ce967a0260d262c3bd2e12b0e2d500a3040eb1ff49aa463ed055b0d61cb72d3a4c6e7a71e8e6c8a3a43751f02ccdec988e696aa800c21fa0477aa72a9aae8689f78016ab12206b7ed2926358bc513023a11e8d04757f3c28c9cf2643c6f14cb6520beb14fa1a55af50ea2b0453a9adabd0b8ae91eb4f7086804f616961746616030fa9f97a117d870fb5e07d992efcaf9ad151b325407fc4a8e9c15b706860fc671f5b9c72014489fd2a43bcbf1c8360991407320268de1217b793a4b2d2e3018486add33e05429976dea60a8e16eac6f835783cb8d1447cd0dc07a606ce05860600ffbafef606d27990b169806fb8fd912e36bbc55994371ce979d5d31c67ee8b0cdde89272ace7b0e0c05773804d78f5c8918b1e639f3e14739cba5a9b9f0eb6adc1f51a982221878b54a83b5c2c50ac149b4d659b3c8beaae487f14af23c1ad4bbdaed28d94b56a166230d02bfa301e359c02d8442f08c6031819e5f24ae49e1a16b60c2803caf09959ab9bd4ab13d6b4838393058e2bad15119493b2120c191652304a29b871df9b56f727237ce939860cfcb3b45945822c4ee6102af4b814dc1f163861ec11588e181a2ca85bb4a4787cc82a35ba3966f4e4731f6d378fbce12c82f6272587df2193fdf6c542899fd6a1c6a3c3c0b60b48c6c4a156572dc9c6cdf2169b1e4b157f8f48e1f79051ccc2e636f7d58c8f947a10e9e3932fccf841a9fb2f99ad082cd70010babae37a158e1c9caec02ef18f1b0fe05d04ad402d14ecb7df18000d5a2704963f7ee02cdace9a33ca445e3808c069ff0f9906fd0864d7dd721f43732756f5e62de0986715f687a2c76d5fa7f9992b22ba6643ca20881213ff940f48040f0d77e2514c744691d73186eef8d88f76ba067600a3a57f350534976a71b8fc42d364c7094ff56f0c00684814a8ee9740b2800d4717be680086e16bb519d70227d2a16b5e3d8e85c182c0d1b194ca9fde5d8255dedc34a6d46127a6ce0368b07b00444d1f2d531196d6425c63be414783b1b528b5c4dc3bbaa21a038498000f8823128f0b32970ee6e9dc5d74449ac75b895e6c544593c1e1b86e2fd3a7ee48e2c6808297f48e68a684bac7383c08d523bc5fbee4c5a6251c5a2292c71975fafd48fb70622f413d13a394e3c99ddd8350ca8ee65b4ab22424110b4ea2b4248df518d868eab2043d69daafbd52885c811d3381b913325e20b4")

            ;

    UserProfile PROFILE_PAUL = new UserProfile()
            .setAboutMe("Made a midnight ride")
            .setEncryptedPrivateKeyInitializationVectorHex("89801187a0ebf05949fecd83426c1a78")
            .setPublicKeyHex("30820122300d06092a864886f70d01010105000382010f003082010a0282010100b67406c4dfa2d504d38dc5e342d495015849235bffb0430f65c2396c0980463745514edc1718d75146c5071feda85cfd763a8cefa78ffcf0a806412f8d9fa9c9761bf3daebbe828a2a6cf07a894d2eab0db620102473bb54571ba2fdfd88c62223dee0c9a70a47e1becb8ffd68eacb7879c06d579d5540b0013704a0a6a7bf6c2f078ea6ee80f6dd923a4bdcbb613367f8f186d6420e39b457b525e1c24665a39a2e2ebd838a750fe131440b3921631cd930c5c7afde98176d62b2094ea540d3e7e2967fc05eed253e53951a65c8cab0f425cf8d55f5743151f36034ded04adf1bcc2bd5834fb8e3cce395f3d9c144e9b2583dbb772a2dbd27e7597bc4ba51db0203010001")
            .setEncryptedPrivateKeyHex("92376185da9cf2b5fd07e184fa4ad9d9ef955801b48cd859515d63176ff52e7cd9da4c22554911bf0960c051cf70f63f8bdfeb894715d1a1ee498da26516293951a802fd6f79345e4aab9a6b47a8b7690bf24d76dda81eb6e0e1fcc279a617fa668d491c272ff140c61c20d4bd04912ba1ced82e2bdb81ae5427f17fc6587dcbb6cf062fbd147ebbe70068943e0527f64eaaf9cfa2e2bf1767964746a3eaeb186cf5356b7fe2e3a882aa9fdb648b66634e5e9c153eb76831c6c078b98f6d19829ba90ab3bb96d0445b8f4ca24231328de8c0d454bf44694a49f0ff335b97ff252d7d1a1a67fdb4081e07c4aeae6c57eb7cd0788104f28ac25224c382d81c8bd4b85591ba82a1d17549d187c730074fb770171c7ee2d6dd6f6fe9ad4abb65170346264c04593a49baaea1213fa340db2dd4c2776e2a09a0398fc2d2e298b03866231000ea881f30ad01af84f5fe8751554c135062c94d24aba82af9247acd272bb7215060b30d916e89142ceecf5e31dbfd28a49b3eb0191bae7ad7137942d316e7f456e2ecb4faccd23d22974c60879662d1fc10a0f7d538fdf25fbe3daf168de52594833b80767fbfe65bcf8a3357eeb48231169c748229de31874aed54e0cfa1f40eece0646d7252e024db98b7debf2d0d74a94eaf9ea395c9aaf467682ec775e0cd26196da51037916626f15bb496631cc7bc7031d704b730819db30f2e6876c7334421a80c02db0119a1f7aa3f7b060bf7bd9ddefe3bbfd3f3fe63f10cc2f6aee4d0f002ac18dedd5818216a38968405127242594628c25fb6bf0f75bdde2220997d2ee5f1d1c78b4fc968a481b3db5bffc50721e0a7d84689f5d00bf21d91dbc025c8add23883c420ce390fe2ac4541f581da200d02dff9441716e520c25a59705fea0c493f1e84c5fad1783c4929f8df0672eb58f8407e5b88a42246a7eadfd371ce5e2ca33d66fff287ced3d231da6f5318c6468abf02f9be1761f0111c67ba10efa87b7aa499b06129136f45eaabb7e1e888c6788d5dab71ff495812953f8e844990e5767476c690eb6c040e2580f8c36a23c7313d4bd1c3462e83fe6e4bae6716ee08863186cee919e400ea951d190a40beecb04a40c37d3605ae7e30b4fd7ed460a1217ec415220d27652be0552f4190d8f68170edf7af49d267baa6607312d7abe5bf99eaf31ad6b68348d186459e1c1273dec7da14bde836c21b3bdf37394f977fdaa1a873065d22bf86e60b2676f212790a7e333fcfddd6cbfd145c382f7a91d595a7030f360d52d260d64d37617c83c8d3964759d8b2e9acccfc3d144495a0ba308b8f0b7f1ac432c90e6d7387500134304f30072d9ba10942f6b5a67d7004e0093b02dcb6fb537a709413eac246b1e76933aeea0d54ad65c81678c42509c514d1f29349842f7fe01cf95df5c2d2a1417dba6a9478032f3425ce21c63d557e4d928cf5a6e9041388d818552c6c4a049c3fe60de7588d365b2861b35f3407fb06a03c4e2c4fa37f615ea73ee86f665d86dfd3ce421a7bed5242877f44e0c591498deb25b0d35b2aef7451895fc77e0f95317fc3011bddc4b2c656d82227ce3ac8bdcd2867fe3c187e1b3f4a35c0a39f23d4678676f2ce4b87b3e4bedbb169670612edb5017c9db0428f52d33bb54a1fab1dfc299b6d3e0ec8520f76653fe82d4b9b7072cc51ec4f30da879788fa5b3a9b9e94df2d53939191bf58034404eee618a51b6499fa86d19a9a")
            ;



}
