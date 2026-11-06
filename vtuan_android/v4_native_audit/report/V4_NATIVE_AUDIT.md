# V4 native feature candidate audit

Verified libysmteam.so SHA-256: \`752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38\`
Native bytes: 9,055,952; ARM64 instructions decoded: 785,928

**Important:** All hexadecimal addresses below are *library-relative RVAs*. They are not proven offsets to game functions or working hooks.

Candidate strings: 113; exports: 5493

## Candidate menu/feature references

| String RVA | Text (truncated) | ARM64 code-reference RVAs |
|---|---|---|
| \`0x408e8\` | ASN1_VISIBLESTRING_free | not found |
| \`0x40900\` | ASN1_VISIBLESTRING_it | not found |
| \`0x40916\` | ASN1_VISIBLESTRING_new | not found |
| \`0x40b01\` | d2i_ASN1_VISIBLESTRING | not found |
| \`0x40c90\` | i2d_ASN1_VISIBLESTRING | not found |
| \`0x3cd300\` | Aim HP Lowest | not found |
| \`0x3cd30e\` | Aim HP % | 0x3bcbd4, 0x3bcbdc, 0x3bf308, 0x3bf310 |
| \`0x3cd37f\` | ng Aim | not found |
| \`0x3cd3cf\` | [HideSkinLSD] player=%u hero=%u skin=%u -> %u | 0xe72b0, 0xe72b8, 0x289eb8, 0x289ec0, 0x3bdd48, 0x3bdd4c |
| \`0x3cd45d\` | ResHeroSkin | 0xebe84, 0xebe94 |
| \`0x3cd488\` | HeroSelectNormalWindow | 0xebeb4, 0xebec4, 0xebee4, 0xebef4 |
| \`0x3cd49f\` | RefreshHeroPanel | 0xebeb8, 0xebec8 |
| \`0x3cd4b0\` | OnClickSelectHeroSkin | 0xebee8, 0xebef8, 0xebf3c, 0xebf48, 0xebf60, 0xebf6c |
| \`0x3cd4d0\` | IsHaveHeroSkin | 0xebf84, 0xebf98 |
| \`0x3cd4f4\` | IsHaveHeroSkin(bool) | 0xebfe4, 0xebff0, 0xec008, 0xec014 |
| \`0x3cd528\` | GetHeroSkin | 0xec164, 0xec174, 0xec1b8, 0xec1c4, 0xec1dc, 0xec1e8 |
| \`0x3cd570\` | ConvertServerHeroInfo | 0xec200, 0xec210, 0xec254, 0xec260, 0xec278, 0xec284 |
| \`0x3cd5cc\` | SkillControlIndicator | 0xef2a0, 0xef2b0, 0x118c6c, 0x118c70, 0x11c0a0, 0x11c0b0, 0x11c0d8, 0x11c0e8, 0x11c104, 0x11c114 |
| \`0x3cd5e2\` | GetUseSkillDirection | 0xef2a4, 0xef2b4 |
| \`0x3cf1c6\` | N AIM | not found |
| \`0x3cf24f\` | aimSkills | 0x1064b0, 0x1064b4 |
| \`0x3cf259\` | Skill 1 | 0x1064c8, 0x1064d0 |
| \`0x3cf261\` | Skill 2 | 0x1064e0, 0x1064e8 |
| \`0x3cf269\` | Skill 3 | 0x1064f4, 0x1064fc |
| \`0x3cf271\` | KC Aim | 0x106568, 0x10656c |
| \`0x3cf278\` | ##DistanceAim | 0x106594, 0x1065a4 |
| \`0x3cf375\` | autoHeroes | 0x10d10c, 0x10d110 |
| \`0x3cf406\` | Aim C1  | 0x10d1c4, 0x10d1cc |
| \`0x3cf548\` | FlorentinoTarget | 0x10d2c8, 0x10d2cc |
| \`0x3cf722\` | BijanTargetPicker | 0x10d414, 0x10d418, 0x10d424, 0x10d428 |
| \`0x3cf734\` | BijanTarget | 0x10d43c, 0x10d440 |
| \`0x3cf9b6\` | Prefab_Hero/ | 0xe20b4, 0xe20b8, 0xe20d0, 0xe20e0 |
| \`0x5270d6\` | CSkillButtonManager | 0x1180dc, 0x1180e0, 0x1181a8, 0x1181ac, 0x11bc3c, 0x11bc4c, 0x11bc78, 0x11bc88, 0x11bca4, 0x11bcb4 |
| \`0x5270f6\` | Native aim callback unavailable; Auto skills remain independent | 0x11b684, 0x11b68c |
| \`0x527212\` | m_currentSkillSlotType | 0x11bd0c, 0x11bd1c, 0x11bd48, 0x11bd58, 0x11bfd8, 0x11bfe8, 0x11c010, 0x11c020 |
| \`0x527236\` | <m_skillButtonManager>k__BackingField | 0x11be40, 0x11be50, 0x11be78, 0x11be88 |
| \`0x52725c\` | SkillControl | 0x11bea4, 0x11beb4, 0x11bee0, 0x11bef0, 0x11cbfc, 0x11cc0c, 0x11cc38, 0x11cc48 |
| \`0x527269\` | SkillSlotArray | 0x11bf0c, 0x11bf1c, 0x11bf44, 0x11bf54 |
| \`0x527278\` | skillIndicator | 0x11c03c, 0x11c04c, 0x11c078, 0x11c088 |
| \`0x527287\` | skillSlot | 0x11c0a4, 0x11c0b4, 0x11c0dc, 0x11c0ec |
| \`0x527291\` | useSkillDirection | 0x11c108, 0x11c118, 0x11c144, 0x11c154 |
| \`0x5272a3\` | isSkillCursorInCancelArea | 0x11c170, 0x11c180, 0x11c1a8, 0x11c1b8 |
| \`0x527307\` | get_bVisible | 0x11c3d8, 0x11c3e8, 0x11c410, 0x11c420 |
| \`0x5273e4\` | CanRequestSkill | 0x11c880, 0x11c890, 0x11c8b8, 0x11c8c8 |
| \`0x5273f4\` | ReadyUseSkillSlot | 0x11c8e8, 0x11c8f8, 0x11c924, 0x11c934 |
| \`0x527406\` | RequestUseSkillSlot | 0x11c954, 0x11c964, 0x11c9a0, 0x11c9b0 |
| \`0x527527\` | LSkillComponent | 0x118dec, 0x118df0, 0x11bf08, 0x11bf18, 0x11bf40, 0x11bf50, 0x11cd28, 0x11cd3c, 0x11cd64, 0x11cd74 |
| \`0x527544\` | LHeroWrapper | 0x11cc60, 0x11cc70, 0x11cc98, 0x11cca8, 0x12aa50, 0x12aa54 |
| \`0x527708\` | input_or_target_changed | 0x120768, 0x12077c, 0x12dff8, 0x12dffc, 0x12f8a8, 0x12f8b0 |
| \`0x52779e\` | target_unavailable | 0x130f38, 0x130f44 |
| \`0x527b53\` | manual_target | 0x138b94, 0x138b98 |
| \`0x527b8b\` | no_reachable_target | 0x138f1c, 0x138f24 |
| \`0x527b9f\` | keep_target | 0x138f00, 0x138f04, 0x139864, 0x139868 |
| \`0x527bab\` | commit_target | 0x138f84, 0x138f88 |
| \`0x527bcb\` | finish_close_target | 0x1391bc, 0x1391c0, 0x139870, 0x139874 |
| \`0x527c15\` | replace_missing_target | 0x139888, 0x13988c |
| \`0x527c2c\` | no_visible_target | 0x1398b0, 0x1398b4 |
| \`0x527c3e\` | commit_current_target | 0x1398ec, 0x1398f0 |
| \`0x527c77\` | 521_florentino/skill/a4 | 0x13e010, 0x13e014 |
| \`0x527c8f\` | 521_florentino/skill/a4.xml | 0x13e12c, 0x13e130 |
| \`0x527cab\` | 521_florentino/skill/ | 0x13e5d8, 0x13e5dc |
| \`0x527ceb\` | BulletSkill | 0x11dbc4, 0x11dbd4, 0x11dc00, 0x11dc10, 0x133898, 0x13389c |
| \`0x527d6b\` | SkillButton | 0x11d390, 0x11d3a0, 0x11d3c8, 0x11d3d8, 0x13a1a8, 0x13a1ac |
| \`0x527eda\` | target_or_owner_changed | 0x13aff0, 0x13affc |
| \`0x527ef2\` | c1_cooldown_confirmed | 0x13afbc, 0x13afc4 |
| \`0x5283f4\` | <m_hostCtrlHero>k__BackingField | 0x11cacc, 0x11cadc, 0x11cb04, 0x11cb14 |
| \`0x528439\` | AsHero | 0x11ccc8, 0x11ccd8, 0x11ccfc, 0x11cd0c |
| \`0x5284a7\` | GetEquipActiveSkillCD | 0x11cedc, 0x11cef0, 0x11cf18, 0x11cf28 |
| \`0x528500\` | SendPlayerChoseEquipSkillCommand | 0x11d09c, 0x11d0b0, 0x11d0dc, 0x11d0ec |
| \`0x52854a\` | CHeroInfo | 0x11d114, 0x11d130, 0x11d160, 0x11d170 |
| \`0x528554\` | GetHeroName | 0x11d118, 0x11d134, 0x11d164, 0x11d174 |
| \`0x52856a\` | _formSkillBtnScript | 0x11d1fc, 0x11d20c, 0x11d234, 0x11d244 |
| \`0x5285c8\` | _skillInfoData | 0x11d3f8, 0x11d408, 0x11d434, 0x11d444 |
| \`0x5285ea\` | SkillStateData | 0x11d45c, 0x11d468 |
| \`0x5285f9\` | curSkillCD | 0x11d46c, 0x11d470 |
| \`0x528604\` | curSkillId | 0x11d490, 0x11d498 |
| \`0x52860f\` | skillLevel | 0x11d4b0, 0x11d4b8 |
| \`0x528709\` | m_bulletSkill | 0x11dafc, 0x11db0c, 0x11db38, 0x11db48 |
| \`0x528717\` | BaseSkill | 0x11db60, 0x11db70, 0x11db98, 0x11dba8 |
| \`0x52879d\` | ForceDisableSkill | 0x11dfd0, 0x11dfe0, 0x11e008, 0x11e018, 0x11e034, 0x11e044, 0x11e070, 0x11e080 |
| \`0x528902\` | GetSkillAttackRange | 0x11e9ac, 0x11e9c0, 0x11e9e8, 0x11e9f8 |
| \`0x528916\` | GetSkillAttackGuideRange | 0x11ea18, 0x11ea2c, 0x11ea54, 0x11ea64 |
| \`0x52892f\` | SkillButtonUp | 0x11ea84, 0x11ea98, 0x11eac0, 0x11ead0 |
| \`0x52893d\` | CancelUseSkillSlot | 0x118cb4, 0x118cb8, 0x11bf6c, 0x11bf7c, 0x11bfa8, 0x11bfb8, 0x11bfd4, 0x11bfe4, 0x11c00c, 0x11c01c |
| \`0x528968\` | SkillButtonDown | 0x11ec2c, 0x11ec3c |
| \`0x528984\` | GetUseSkillTargetDefaultAttackMode | 0x11ec84, 0x11ec8c |
| \`0x5289a7\` | GetUseSkillTargetLockAttackMode | 0x11eca4, 0x11ecac |
| \`0x5289c7\` | GetUseSkillPosition | 0x11ecc4, 0x11eccc |
| \`0x5289db\` | HeroRuntime | 0x11d1bc, 0x11d1cc, 0x11d228, 0x11d238, 0x11d290, 0x11d2a0, 0x11d2f4, 0x11d304, 0x11d35c, 0x11d36c |
| \`0x528d5e\` | ##BijanTargetHud | 0x1562f8, 0x156300 |
| \`0x79ecfb\` | HeroSettings | 0x189eb0, 0x189eb4, 0x189ec0, 0x189ec4, 0x189ee0, 0x189ee4, 0x19a8e4, 0x19a8e8 |
| \`0x79ed08\` | HeroOptions | 0x189f14, 0x189f18, 0x189f24, 0x189f28, 0x189f40, 0x189f44, 0x19a93c, 0x19a940 |
| \`0x7a00f5\` | DragDropTarget | not found |
| \`0x7a0104\` | DragDropTargetBg | not found |
| \`0x7a0312\` | [io] Calling Platform_SetImeDataFn(): WantVisible: %d, InputPos (%.2f,%. | 0x1f91c4, 0x1f91d0 |
| \`0x7a0911\` | Programmer error: %d visible items with conflicting ID! | 0x1f9cb8, 0x1f9cbc |
| \`0x7a1493\` | %d visible windows, %d current allocations | not found |
| \`0x7a1ae1\` | Windows Minimap | not found |
| \`0x7a22da\` | NavActive: %d, NavVisible: %d | not found |
| \`0x7a233e\` | NavCursorVisible: %d, NavHighlightItemUnderNav: %d | not found |
| \`0x7a23b4\` | NavWindowingTarget: '%s' | not found |
| \`0x7a4711\` | Enabled: %d, VisibleX/Y: %d/%d, RequestOutput: %d, SkipItems: %d, DrawCh | not found |
| \`0x7a4984\` | Visible=%d%n | 0x20d11c, 0x20d120 |
| \`0x7a49f6\` |  Visible=%d | 0x20d304, 0x20d320 |
| \`0x7ade96\` | ac-targeting | not found |
| \`0x7ae745\` | targetInformation | not found |
| \`0x7ae757\` | X509v3 AC Targeting | not found |
| \`0x7b9df1\` | ASN1_VISIBLESTRING | not found |
| \`0x7be7f7\` | SSL: no alternative certificate subject name matches target host name '% | 0x2b2d70, 0x2b2d74 |
| \`0x7be8b9\` | SSL: certificate subject name '%s' does not match target host name '%s' | 0x2b3550, 0x2b3554 |
| \`0x7d3252\` | VISIBLE | not found |
| \`0x7db8be\` | encipherOnly | not found |
| \`0x7db8d9\` | decipherOnly | not found |

## Exported native functions

- \`0x2c6998\` \`SSL_set_connect_state\`
- \`0x2aa3dc\` \`rtsp_connisdead\`
- \`0x2f6e64\` \`ENGINE_get_load_privkey_function\`
- \`0x2e2a3c\` \`DH_up_ref\`
- \`0x352d7c\` \`d2i_CMS_ContentInfo\`
- \`0x2ebf64\` \`EC_GROUP_free\`
- \`0x319cd4\` \`X509_OBJECT_idx_by_subject\`
- \`0x31d750\` \`X509_STORE_CTX_get_ex_data\`
- \`0x28ff7c\` \`int_rsa_verify\`
- \`0x37aca0\` \`_ZNSt6__ndk18ios_base17register_callbackEPFvNS0_5eventERS0_iEi\`
- \`0x28ca18\` \`EVP_PKEY_meth_set_verify_recover\`
- \`0x352e94\` \`cms_content_bio\`
- \`0x2f789c\` \`ENGINE_set_DH\`
- \`0x31acf0\` \`X509_REQ_get_attr_count\`
- \`0x328510\` \`i2d_PKEY_USAGE_PERIOD\`
- \`0x352168\` \`CMS_stream\`
- \`0x31d980\` \`X509_STORE_CTX_free\`
- \`0x38b530\` \`_ZNKSt6__ndk110moneypunctIwLb0EE13do_pos_formatEv\`
- \`0x2815cc\` \`curlx_uztosz\`
- \`0x2a6fe4\` \`Curl_llist_init\`
- \`0x26ac08\` \`Curl_ipv6works\`
- \`0x2d2820\` \`SSL_CTX_set_srp_username\`
- \`0x38b5dc\` \`_ZNKSt6__ndk117moneypunct_bynameIcLb0EE16do_decimal_pointEv\`
- \`0x272e28\` \`Curl_ratelimit\`
- \`0x30e6bc\` \`RSA_OAEP_PARAMS_new\`
- \`0x29fd9c\` \`Curl_output_ntlm\`
- \`0x33f01c\` \`ASN1_i2d_bio\`
- \`0x2a3b80\` \`Curl_add_timecondition\`
- \`0x2fcc44\` \`EVP_PKEY_decrypt\`
- \`0x26a838\` \`Curl_hostcache_clean\`
- \`0x2c6a60\` \`SSL_peek\`
- \`0x28b954\` \`ENGINE_get_pkey_asn1_meth_str\`
- \`0x2f6640\` \`ENGINE_register_all_complete\`
- \`0x28c134\` \`ENGINE_unregister_RAND\`
- \`0x26472c\` \`CRYPTO_gcm128_decrypt_ctr32\`
- \`0x2907d8\` \`X509V3_add_value_bool\`
- \`0x3b654c\` \`_ZNSt10bad_typeidC1Ev\`
- \`0x31772c\` \`X509_LOOKUP_file\`
- \`0x3aefec\` \`_ZNSt6__ndk14stofERKNS_12basic_stringIwNS_11char_traitsIwEENS_9allocatorIwEEEEPm\`
- \`0x2dff68\` \`COMP_compress_block\`
- \`0x3b3710\` \`_ZSt15set_new_handlerPFvvE\`
- \`0x3a952c\` \`_ZNSt6__ndk119__shared_weak_count14__release_weakEv\`
- \`0x2f8e24\` \`EVP_bf_ofb\`
- \`0x2f7af0\` \`ENGINE_unregister_DSA\`
- \`0x3294d0\` \`X509_PURPOSE_get0\`
- \`0x2dfa4c\` \`CMAC_Init\`
- \`0x37b12c\` \`_ZNKSt6__ndk17collateIcE10do_compareEPKcS3_S3_S3_\`
- \`0x326378\` \`i2d_AUTHORITY_INFO_ACCESS\`
- \`0x2c943c\` \`SSL_set_debug\`
- \`0x286e40\` \`ASN1_IA5STRING_new\`
- \`0x39f350\` \`_ZNSt6__ndk18numpunctIcEC2Em\`
- \`0x34c1ac\` \`bn_sub_part_words\`
- \`0x315d90\` \`ERR_load_UI_strings\`
- \`0x2a0c34\` \`Curl_http_setup_conn\`
- \`0x33dc54\` \`c2i_ASN1_BIT_STRING\`
- \`0x355f5c\` \`CONF_load\`
- \`0x325e6c\` \`GENERAL_NAME_set0_value\`
- \`0x3186cc\` \`X509_issuer_name_cmp\`
- \`0x3af1d4\` \`_ZNSt6__ndk14stodERKNS_12basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEEEPm\`
- \`0x36fa1c\` \`_ZNSt6__ndk115basic_streambufIwNS_11char_traitsIwEEE7sungetcEv\`
- \`0x293d84\` \`Curl_timeleft\`
- \`0x2ff5f8\` \`OBJ_add_sigid\`
- \`0x318e88\` \`X509_CRL_check_suiteb\`
- \`0x2d2ad8\` \`i2d_ASN1_bytes\`
- \`0x2cc870\` \`tls1_default_timeout\`
- \`0x2fb2c8\` \`EVP_PKEY_get_attr_by_NID\`
- \`0x28f368\` \`RSA_verify_PKCS1_PSS_mgf1\`
- \`0x3188c8\` \`X509_subject_name_hash\`
- \`0x319278\` \`X509_REVOKED_get_ext_d2i\`
- \`0x3b34fc\` \`__cxa_guard_release\`
- \`0x25a7dc\` \`ERR_get_error\`
- \`0x36e11c\` \`_ZNSt6__ndk17promiseIvEC2Ev\`
- \`0x2907f4\` \`X509V3_add_value_bool_nf\`
- \`0x36d438\` \`_ZNSt6__ndk16chrono12system_clock9to_time_tERKNS0_10time_pointIS1_NS0_8durationIxNS_5ratioILl1ELl1000000EEEEEEE\`
- \`0x35c5b0\` \`ec_GF2m_simple_group_init\`
- \`0x2e0890\` \`CONF_module_set_usr_data\`
- \`0x2d69dc\` \`X509_signature_dump\`
- \`0x36ee1c\` \`_ZNSt6__ndk18ios_baseD1Ev\`
- \`0x2de8e0\` \`bn_mul_comba4\`
- \`0x28ae98\` \`BUF_memdup\`
- \`0x361204\` \`PEM_write_bio_PKCS8PrivateKey\`
- \`0x29f850\` \`Curl_httpchunk_read\`
- \`0x2ebce0\` \`EC_KEY_get_conv_form\`
- \`0x2c1784\` \`ssl3_dispatch_alert\`
- \`0x39981c\` \`_ZNSt6__ndk114collate_bynameIwEC2EPKcm\`
- \`0x345670\` \`NETSCAPE_SPKI_new\`
- \`0x2de28c\` \`bn_mul_comba8\`
- \`0x3a9f2c\` \`_ZdlPvm\`
- \`0x316c14\` \`UI_method_set_prompt_constructor\`
- \`0x288e58\` \`BN_num_bits\`
- \`0x38d080\` \`_ZNSt6__ndk111__money_getIcE13__gather_infoEbRKNS_6localeERNS_10money_base7patternERcS8_RNS_12basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEEESF_SF_SF_Ri\`
- \`0x2746e8\` \`Curl_vsetopt\`
- \`0x369c60\` \`BN_GF2m_mod_exp\`
- \`0x3a95a4\` \`_ZNKSt6__ndk119__shared_weak_count13__get_deleterERKSt9type_info\`
- \`0x361560\` \`PEM_write_PKCS8PrivateKey_nid\`
- \`0x32ae44\` \`Curl_fnmatch\`
- \`0x3207f4\` \`d2i_X509_bio\`
- \`0x2c584c\` \`ssl_clear_hash_ctx\`
- \`0x36cca8\` \`BIO_asn1_set_suffix\`
- \`0x306660\` \`d2i_PKCS12_fp\`
- \`0x2df968\` \`CMAC_CTX_get0_cipher_ctx\`
- \`0x325d94\` \`GENERAL_NAME_cmp\`
- \`0x2c72ac\` \`ssl_cipher_list_to_bytes\`
- \`0x2cc290\` \`PEM_write_bio_SSL_SESSION\`
- \`0x2dac9c\` \`X509_NAME_new\`
- \`0x36f2c0\` \`_ZNSt6__ndk19basic_iosIwNS_11char_traitsIwEEED2Ev\`
- \`0x29f480\` \`Curl_hash_pick\`
- \`0x348b84\` \`BN_mod_exp_mont\`
- \`0x27d578\` \`Curl_connected_proxy\`
- \`0x320f64\` \`i2d_PKCS8_PRIV_KEY_INFO_fp\`
- \`0x2a71e0\` \`Curl_md5it\`
- \`0x372a98\` \`_ZNSt6__ndk113basic_istreamIcNS_11char_traitsIcEEE5seekgENS_4fposI9mbstate_tEE\`
- \`0x39f42c\` \`_ZNSt6__ndk115numpunct_bynameIwED1Ev\`
- \`0x2eaa50\` \`d2i_ECParameters\`
- \`0x399244\` \`_ZNSt6__ndk114collate_bynameIcEC1ERKNS_12basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEEEm\`
- \`0x31ad08\` \`X509_REQ_delete_attr\`
- \`0x2608c0\` \`lh_num_items\`
- \`0x2bfb00\` \`ssl3_renegotiate_check\`
- \`0x39b62c\` \`_ZNKSt6__ndk112ctype_bynameIwE9do_narrowEwc\`
- \`0x351ecc\` \`cms_EnvelopedData_init_bio\`
- \`0x2f5f7c\` \`ENGINE_ctrl_cmd\`
- \`0x316aa4\` \`UI_get_ex_new_index\`
- \`0x2731d8\` \`Curl_rand\`
- \`0x39d724\` \`_ZNKSt6__ndk114__codecvt_utf8IDsE16do_always_noconvEv\`
- \`0x3a0d70\` \`_ZNKSt6__ndk120__time_get_c_storageIcE3__rEv\`
- \`0x31761c\` \`OPENSSL_issetugid\`
- \`0x2e2730\` \`DH_set_default_method\`
- \`0x2610d8\` \`CRYPTO_free\`
- \`0x2c2198\` \`ssl_cert_set0_chain\`
- \`0x2e4568\` \`DSA_verify\`
- \`0x39896c\` \`_ZNSt6__ndk16localeC1ERKNS_12basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEEE\`
- \`0x26ea14\` \`curl_mvfprintf\`
- \`0x31f420\` \`X509_VERIFY_PARAM_add0_policy\`
- \`0x37b104\` \`_ZNSt6__ndk17collateIcED1Ev\`
- \`0x3694bc\` \`BN_GF2m_mod_inv\`
- \`0x2c9640\` \`SSL_use_certificate_file\`
- \`0x279e1c\` \`curl_share_setopt\`
- \`0x309e94\` \`RAND_file_name\`
- \`0x32feb8\` \`dtls1_connect\`
- \`0x30f0b0\` \`RSA_new\`
- \`0x2dbe58\` \`X509_alias_set1\`
- \`0x35d178\` \`ec_GF2m_simple_is_on_curve\`
- \`0x2a712c\` \`Curl_llist_count\`
- \`0x3500a0\` \`bn_sqr_normal\`
- \`0x2e6eec\` \`DSO_get_method\`
- \`0x269fcc\` \`Curl_printable_address\`
- \`0x3456ac\` \`X509_VAL_free\`
- \`0x3a9418\` \`_ZNSt6__ndk114__shared_countD2Ev\`
- \`0x3b2ed4\` \`__cxa_current_exception_type\`
- \`0x2ff7b0\` \`d2i_OCSP_SIGNATURE\`
- \`0x2e0850\` \`CONF_imodule_get_name\`
- \`0x354748\` \`cms_SignerIdentifier_cert_cmp\`
- \`0x2e0880\` \`CONF_imodule_set_flags\`
- \`0x35a2c8\` \`DES_set_odd_parity\`
- \`0x2815b8\` \`curlx_uztoui\`
- \`0x38eec4\` \`_ZNSt6__ndk111__money_getIwE13__gather_infoEbRKNS_6localeERNS_10money_base7patternERwS8_RNS_12basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEEERNS9_IwNSA_IwEENSC_IwEEEESJ_SJ_Ri\`
- \`0x25c79c\` \`EVP_aes_128_cfb128\`
- \`0x2ec57c\` \`EC_POINT_copy\`
- \`0x323c90\` \`X509V3_get_string\`
- \`0x359660\` \`DES_ncbc_encrypt\`
- \`0x2c8d94\` \`SSL_CTX_get0_privatekey\`
- \`0x2e0038\` \`CONF_modules_load\`
- \`0x286d50\` \`ASN1_OCTET_STRING_new\`
- \`0x285510\` \`i2a_ASN1_OBJECT\`
- \`0x361ddc\` \`PKCS12_decrypt_skey\`
- \`0x37b2a4\` \`_ZNSt6__ndk17collateIwED2Ev\`
- \`0x286cb4\` \`i2d_ASN1_INTEGER\`
- \`0x32feac\` \`DTLSv1_client_method\`
- \`0x2815b0\` \`curlx_uztoul\`
- \`0x299bc8\` \`Curl_fileinfo_cleanup\`
- \`0x3a0e08\` \`_ZNKSt6__ndk120__time_get_c_storageIwE3__rEv\`
- \`0x29dfb4\` \`Curl_ftp_parselist_data_free\`
- \`0x324a8c\` \`d2i_NOTICEREF\`
- \`0x36ffc0\` \`_ZNSt6__ndk113basic_istreamIcNS_11char_traitsIcEEE4swapERS3_\`
- \`0x2caf4c\` \`SSL_SESSION_new\`
- \`0x32f6ec\` \`dtls1_shutdown\`
- \`0x301c70\` \`PEM_write_bio_NETSCAPE_CERT_SEQUENCE\`
- \`0x377a50\` \`_ZNSt6__ndk113basic_ostreamIcNS_11char_traitsIcEEElsEPKv\`
- \`0x3784c8\` \`_ZNSt6__ndk113basic_ostreamIwNS_11char_traitsIwEEElsEPFRNS_8ios_baseES5_E\`
- \`0x30e6b0\` \`i2d_RSA_OAEP_PARAMS\`
- \`0x27251c\` \`Curl_pipeline_set_server_blacklist\`
- \`0x2f45a0\` \`ECDSA_METHOD_get_app_data\`
- \`0x27ab70\` \`Curl_saferealloc\`
- \`0x35ee54\` \`EVP_DigestVerifyFinal\`
- \`0x28ab20\` \`BN_add_word\`
- \`0x25c7cc\` \`EVP_aes_192_cbc\`
- \`0x28a410\` \`BN_print\`
- \`0x3b638c\` \`_ZNSt12out_of_rangeD0Ev\`
- \`0x36afc0\` \`_CONF_free_data\`
- \`0x3766b0\` \`_ZNSt6__ndk18ios_base33__set_badbit_and_consider_rethrowEv\`
- \`0x2c6960\` \`SSL_connect\`
- \`0x259058\` \`BIO_s_mem\`
- \`0x25987c\` \`CRYPTO_get_new_dynlockid\`
- \`0x259ecc\` \`CRYPTO_set_id_callback\`
- \`0x2da5c4\` \`X509_ATTRIBUTE_new\`
- \`0x3b18ec\` \`_ZNSt6__ndk112system_errorC1ENS_10error_codeERKNS_12basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEEE\`
- \`0x3ab920\` \`_ZNKSt6__ndk112basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE7compareEmmPKcm\`
- \`0x320640\` \`NETSCAPE_SPKI_verify\`
- \`0x30968c\` \`PKCS7_cert_from_signer_info\`
- \`0x2d5394\` \`ASN1_parse\`

## What this proves

This audit finds exact embedded labels and instructions referencing them.
It does NOT establish which functions implement ESP/AIM, how the hooks work, or that offsets can be transplanted into another APK. Further data-flow analysis and testing on the specific game build are required.
