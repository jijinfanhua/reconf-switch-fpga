module Processor(
  input         clock,
  input         reset,
  input         io_start,
  input  [7:0]  io_packet_header_0,
  input  [7:0]  io_packet_header_1,
  input  [7:0]  io_packet_header_2,
  input  [7:0]  io_packet_header_3,
  input  [7:0]  io_packet_header_4,
  input  [7:0]  io_packet_header_5,
  input  [7:0]  io_packet_header_6,
  input  [7:0]  io_packet_header_7,
  input  [7:0]  io_packet_header_8,
  input  [7:0]  io_packet_header_9,
  input  [7:0]  io_packet_header_10,
  input  [7:0]  io_packet_header_11,
  input  [7:0]  io_packet_header_12,
  input  [7:0]  io_packet_header_13,
  input  [7:0]  io_packet_header_14,
  input  [7:0]  io_packet_header_15,
  input  [7:0]  io_packet_header_16,
  input  [7:0]  io_packet_header_17,
  input  [7:0]  io_packet_header_18,
  input  [7:0]  io_packet_header_19,
  input  [7:0]  io_packet_header_20,
  input  [7:0]  io_packet_header_21,
  input  [7:0]  io_packet_header_22,
  input  [7:0]  io_packet_header_23,
  input  [7:0]  io_packet_header_24,
  input  [7:0]  io_packet_header_25,
  input  [7:0]  io_packet_header_26,
  input  [7:0]  io_packet_header_27,
  input  [7:0]  io_packet_header_28,
  input  [7:0]  io_packet_header_29,
  input  [7:0]  io_packet_header_30,
  input  [7:0]  io_packet_header_31,
  input  [7:0]  io_packet_header_32,
  input  [7:0]  io_packet_header_33,
  input  [7:0]  io_packet_header_34,
  input  [7:0]  io_packet_header_35,
  input  [7:0]  io_packet_header_36,
  input  [7:0]  io_packet_header_37,
  input  [7:0]  io_packet_header_38,
  input  [7:0]  io_packet_header_39,
  input  [7:0]  io_packet_header_40,
  input  [7:0]  io_packet_header_41,
  input  [7:0]  io_packet_header_42,
  input  [7:0]  io_packet_header_43,
  input  [7:0]  io_packet_header_44,
  input  [7:0]  io_packet_header_45,
  input  [7:0]  io_packet_header_46,
  input  [7:0]  io_packet_header_47,
  input  [7:0]  io_packet_header_48,
  input  [7:0]  io_packet_header_49,
  input  [7:0]  io_packet_header_50,
  input  [7:0]  io_packet_header_51,
  input  [7:0]  io_packet_header_52,
  input  [7:0]  io_packet_header_53,
  input  [7:0]  io_packet_header_54,
  input  [7:0]  io_packet_header_55,
  input  [7:0]  io_packet_header_56,
  input  [7:0]  io_packet_header_57,
  input  [7:0]  io_packet_header_58,
  input  [7:0]  io_packet_header_59,
  input  [7:0]  io_packet_header_60,
  input  [7:0]  io_packet_header_61,
  input  [7:0]  io_packet_header_62,
  input  [7:0]  io_packet_header_63,
  output [31:0] io_mem_addr,
  input  [31:0] io_mem_rdata,
  output        io_ready,
  input         io_mod_start,
  input  [31:0] io_mod_hit_action_addr,
  input  [31:0] io_mod_miss_action_addr,
  input         io_mod_ps_mod_start,
  input         io_mod_ps_mod_header_id,
  input  [31:0] io_mod_ps_mod_header_length,
  input  [31:0] io_mod_ps_mod_next_tag_start,
  input  [31:0] io_mod_ps_mod_next_table_0,
  input  [31:0] io_mod_ps_mod_next_table_1,
  input         io_mod_mt_mod_start,
  input  [3:0]  io_mod_mt_mod_header_id,
  input  [5:0]  io_mod_mt_mod_key_off,
  input  [5:0]  io_mod_mt_mod_key_len,
  input  [5:0]  io_mod_mt_mod_val_len,
  input         io_mod_ex_mod_start,
  input  [63:0] io_mod_ex_mod_ops_0,
  input  [63:0] io_mod_ex_mod_ops_1,
  input  [63:0] io_mod_ex_mod_ops_2,
  input  [63:0] io_mod_ex_mod_ops_3,
  input  [63:0] io_mod_ex_mod_ops_4,
  input  [63:0] io_mod_ex_mod_ops_5,
  input  [63:0] io_mod_ex_mod_ops_6,
  input  [63:0] io_mod_ex_mod_ops_7,
  input  [63:0] io_mod_ex_mod_ops_8,
  input  [63:0] io_mod_ex_mod_ops_9,
  input  [63:0] io_mod_ex_mod_ops_10,
  input  [63:0] io_mod_ex_mod_ops_11,
  input  [63:0] io_mod_ex_mod_ops_12,
  input  [63:0] io_mod_ex_mod_ops_13,
  input  [63:0] io_mod_ex_mod_ops_14,
  input  [63:0] io_mod_ex_mod_ops_15
);

proc_base proc(
    .clk(clock),
    .rst(reset),
    .start_i(io_start),
    .pkt_hdr_i('{
      io_packet_header_0,
      io_packet_header_1,
      io_packet_header_2,
      io_packet_header_3,
      io_packet_header_4,
      io_packet_header_5,
      io_packet_header_6,
      io_packet_header_7,
      io_packet_header_8,
      io_packet_header_9,
      io_packet_header_10,
      io_packet_header_11,
      io_packet_header_12,
      io_packet_header_13,
      io_packet_header_14,
      io_packet_header_15,
      io_packet_header_16,
      io_packet_header_17,
      io_packet_header_18,
      io_packet_header_19,
      io_packet_header_20,
      io_packet_header_21,
      io_packet_header_22,
      io_packet_header_23,
      io_packet_header_24,
      io_packet_header_25,
      io_packet_header_26,
      io_packet_header_27,
      io_packet_header_28,
      io_packet_header_29,
      io_packet_header_30,
      io_packet_header_31,
      io_packet_header_32,
      io_packet_header_33,
      io_packet_header_34,
      io_packet_header_35,
      io_packet_header_36,
      io_packet_header_37,
      io_packet_header_38,
      io_packet_header_39,
      io_packet_header_40,
      io_packet_header_41,
      io_packet_header_42,
      io_packet_header_43,
      io_packet_header_44,
      io_packet_header_45,
      io_packet_header_46,
      io_packet_header_47,
      io_packet_header_48,
      io_packet_header_49,
      io_packet_header_50,
      io_packet_header_51,
      io_packet_header_52,
      io_packet_header_53,
      io_packet_header_54,
      io_packet_header_55,
      io_packet_header_56,
      io_packet_header_57,
      io_packet_header_58,
      io_packet_header_59,
      io_packet_header_60,
      io_packet_header_61,
      io_packet_header_62,
      io_packet_header_63
    }),
    .mem_ce_o(`TRUE),
    .mem_we_o(`FALSE),
    .mem_addr_o(io_mem_addr),
    .mem_width_o(1),
    .mem_data_o(0),
    .mem_data_i(io_mem_rdata),
    .ready_o(io_ready),
    .proc_mod_start_i(io_mod_start),
    .proc_mod_hit_action_addr_i(io_mod_hit_action_addr),
    .proc_mod_miss_action_addr_i(io_mod_miss_action_addr),
    .ps_mod_start_i(io_mod_ps_mod_start),
    .ps_mod_hdr_id_i(io_mod_ps_mod_header_id),
    .ps_mod_hdr_len_i(io_mod_ps_mod_header_length),
    .ps_mod_next_tag_start_i(io_mod_ps_mod_next_tag_start),
    .ps_mod_next_tag_len_i(0),
    .ps_mod_next_table_i('{
      io_mod_ps_mod_next_table_0,
      io_mod_ps_mod_next_table_1
    }),
    .mt_mod_start_i(io_mod_mt_mod_start),
    .mt_mod_match_hdr_id_i(io_mod_mt_mod_header_id),
    .mt_mod_match_key_off_i(io_mod_mt_mod_key_off),
    .mt_mod_match_key_len_i(io_mod_mt_mod_key_len),
    .mt_mod_match_val_len_i(io_mod_mt_mod_val_len),
    .ex_mod_start_i(io_mod_ex_mod_start),
    .ex_mod_ops_i('{
      io_mod_ex_mod_ops_0,
      io_mod_ex_mod_ops_1,
      io_mod_ex_mod_ops_2,
      io_mod_ex_mod_ops_3,
      io_mod_ex_mod_ops_4,
      io_mod_ex_mod_ops_5,
      io_mod_ex_mod_ops_6,
      io_mod_ex_mod_ops_7,
      io_mod_ex_mod_ops_8,
      io_mod_ex_mod_ops_9,
      io_mod_ex_mod_ops_10,
      io_mod_ex_mod_ops_11,
      io_mod_ex_mod_ops_12,
      io_mod_ex_mod_ops_13,
      io_mod_ex_mod_ops_14,
      io_mod_ex_mod_ops_15
    })
);

endmodule