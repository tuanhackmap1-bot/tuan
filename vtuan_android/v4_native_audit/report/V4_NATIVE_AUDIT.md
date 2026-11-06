# V4 native feature candidate audit

Verified libysmteam.so SHA-256: \`752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38\`
Native bytes: 9,055,952; ARM64 instructions decoded: 0

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
| \`0x3cd30e\` | Aim HP % | not found |
| \`0x3cd37f\` | ng Aim | not found |
| \`0x3cd3cf\` | [HideSkinLSD] player=%u hero=%u skin=%u -> %u | not found |
| \`0x3cd45d\` | ResHeroSkin | not found |
| \`0x3cd488\` | HeroSelectNormalWindow | not found |
| \`0x3cd49f\` | RefreshHeroPanel | not found |
| \`0x3cd4b0\` | OnClickSelectHeroSkin | not found |
| \`0x3cd4d0\` | IsHaveHeroSkin | not found |
| \`0x3cd4f4\` | IsHaveHeroSkin(bool) | not found |
| \`0x3cd528\` | GetHeroSkin | not found |
| \`0x3cd570\` | ConvertServerHeroInfo | not found |
| \`0x3cd5cc\` | SkillControlIndicator | not found |
| \`0x3cd5e2\` | GetUseSkillDirection | not found |
| \`0x3cf1c6\` | N AIM | not found |
| \`0x3cf24f\` | aimSkills | not found |
| \`0x3cf259\` | Skill 1 | not found |
| \`0x3cf261\` | Skill 2 | not found |
| \`0x3cf269\` | Skill 3 | not found |
| \`0x3cf271\` | KC Aim | not found |
| \`0x3cf278\` | ##DistanceAim | not found |
| \`0x3cf375\` | autoHeroes | not found |
| \`0x3cf406\` | Aim C1  | not found |
| \`0x3cf548\` | FlorentinoTarget | not found |
| \`0x3cf722\` | BijanTargetPicker | not found |
| \`0x3cf734\` | BijanTarget | not found |
| \`0x3cf9b6\` | Prefab_Hero/ | not found |
| \`0x5270d6\` | CSkillButtonManager | not found |
| \`0x5270f6\` | Native aim callback unavailable; Auto skills remain independent | not found |
| \`0x527212\` | m_currentSkillSlotType | not found |
| \`0x527236\` | <m_skillButtonManager>k__BackingField | not found |
| \`0x52725c\` | SkillControl | not found |
| \`0x527269\` | SkillSlotArray | not found |
| \`0x527278\` | skillIndicator | not found |
| \`0x527287\` | skillSlot | not found |
| \`0x527291\` | useSkillDirection | not found |
| \`0x5272a3\` | isSkillCursorInCancelArea | not found |
| \`0x527307\` | get_bVisible | not found |
| \`0x5273e4\` | CanRequestSkill | not found |
| \`0x5273f4\` | ReadyUseSkillSlot | not found |
| \`0x527406\` | RequestUseSkillSlot | not found |
| \`0x527527\` | LSkillComponent | not found |
| \`0x527544\` | LHeroWrapper | not found |
| \`0x527708\` | input_or_target_changed | not found |
| \`0x52779e\` | target_unavailable | not found |
| \`0x527b53\` | manual_target | not found |
| \`0x527b8b\` | no_reachable_target | not found |
| \`0x527b9f\` | keep_target | not found |
| \`0x527bab\` | commit_target | not found |
| \`0x527bcb\` | finish_close_target | not found |
| \`0x527c15\` | replace_missing_target | not found |
| \`0x527c2c\` | no_visible_target | not found |
| \`0x527c3e\` | commit_current_target | not found |
| \`0x527c77\` | 521_florentino/skill/a4 | not found |
| \`0x527c8f\` | 521_florentino/skill/a4.xml | not found |
| \`0x527cab\` | 521_florentino/skill/ | not found |
| \`0x527ceb\` | BulletSkill | not found |
| \`0x527d6b\` | SkillButton | not found |
| \`0x527eda\` | target_or_owner_changed | not found |
| \`0x527ef2\` | c1_cooldown_confirmed | not found |
| \`0x5283f4\` | <m_hostCtrlHero>k__BackingField | not found |
| \`0x528439\` | AsHero | not found |
| \`0x5284a7\` | GetEquipActiveSkillCD | not found |
| \`0x528500\` | SendPlayerChoseEquipSkillCommand | not found |
| \`0x52854a\` | CHeroInfo | not found |
| \`0x528554\` | GetHeroName | not found |
| \`0x52856a\` | _formSkillBtnScript | not found |
| \`0x5285c8\` | _skillInfoData | not found |
| \`0x5285ea\` | SkillStateData | not found |
| \`0x5285f9\` | curSkillCD | not found |
| \`0x528604\` | curSkillId | not found |
| \`0x52860f\` | skillLevel | not found |
| \`0x528709\` | m_bulletSkill | not found |
| \`0x528717\` | BaseSkill | not found |
| \`0x52879d\` | ForceDisableSkill | not found |
| \`0x528902\` | GetSkillAttackRange | not found |
| \`0x528916\` | GetSkillAttackGuideRange | not found |
| \`0x52892f\` | SkillButtonUp | not found |
| \`0x52893d\` | CancelUseSkillSlot | not found |
| \`0x528968\` | SkillButtonDown | not found |
| \`0x528984\` | GetUseSkillTargetDefaultAttackMode | not found |
| \`0x5289a7\` | GetUseSkillTargetLockAttackMode | not found |
| \`0x5289c7\` | GetUseSkillPosition | not found |
| \`0x5289db\` | HeroRuntime | not found |
| \`0x528d5e\` | ##BijanTargetHud | not found |
| \`0x79ecfb\` | HeroSettings | not found |
| \`0x79ed08\` | HeroOptions | not found |
| \`0x7a00f5\` | DragDropTarget | not found |
| \`0x7a0104\` | DragDropTargetBg | not found |
| \`0x7a0312\` | [io] Calling Platform_SetImeDataFn(): WantVisible: %d, InputPos (%.2f,%. | not found |
| \`0x7a0911\` | Programmer error: %d visible items with conflicting ID! | not found |
| \`0x7a1493\` | %d visible windows, %d current allocations | not found |
| \`0x7a1ae1\` | Windows Minimap | not found |
| \`0x7a22da\` | NavActive: %d, NavVisible: %d | not found |
| \`0x7a233e\` | NavCursorVisible: %d, NavHighlightItemUnderNav: %d | not found |
| \`0x7a23b4\` | NavWindowingTarget: '%s' | not found |
| \`0x7a4711\` | Enabled: %d, VisibleX/Y: %d/%d, RequestOutput: %d, SkipItems: %d, DrawCh | not found |
| \`0x7a4984\` | Visible=%d%n | not found |
| \`0x7a49f6\` |  Visible=%d | not found |
| \`0x7ade96\` | ac-targeting | not found |
| \`0x7ae745\` | targetInformation | not found |
| \`0x7ae757\` | X509v3 AC Targeting | not found |
| \`0x7b9df1\` | ASN1_VISIBLESTRING | not found |
| \`0x7be7f7\` | SSL: no alternative certificate subject name matches target host name '% | not found |
| \`0x7be8b9\` | SSL: certificate subject name '%s' does not match target host name '%s' | not found |
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
