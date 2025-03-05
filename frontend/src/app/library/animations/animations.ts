import { animate, keyframes, state, style, transition, trigger } from "@angular/animations";

export const STEPPER_ANIMATIONS  = [
    trigger('previous_btn',[
        transition(':enter',[
            style({'transform': 'translateX(100%)', 'z-index':'9999', 'opacity':0.4}),
            animate('600ms ease-out', style({
                'transform': 'translateX(0)',
                'opacity':1
            }))
        ]),
        transition(':leave',[
            style({
                'transform': 'translateX(0)',
                'opacity':'1',
                'z-index':'9999'
            }),
            animate('800ms ease-out', keyframes([
                style({'transform': 'translateX(120%)', offset:0.8}),
                style({'opacity': '0.2', offset:0.96})
            ]))
        ])
    ]),
    trigger('next_btn', [
        transition(':leave',[
            style({opacity:1, position:'absolute', 'z-index':'9999'}),
            animate('700ms ease-out', keyframes( [
                style({ transform:'translateX(-100%)', offset:0.6}),
                style({ opacity:'0.2', offset:0.87})
        ]))
        ]),
        transition(':enter',[
            style({opacity:0.3, 'z-index':'9999', transform:'translateX(-100%)'}),
            animate('600ms ease-out', style({ opacity:'1', transform:'translateX(0)' }))
        ])
    ]),
    trigger('finalize_btn',[
        transition(':enter',[
            style({opacity:0.3}),
            animate('400ms ease-in', style({opacity:1}))
        ]),
        transition(':leave',[
            style({opacity:1, position:'absolute'}),
            animate('600ms ease-in', style({opacity:0.3}))
        ])
    ])
];
export const GENERAL_ANIMATIONS = [
    trigger('enterIn',[
        transition(':enter',[
            style({
                transform:'scale(0)',
                'transform-origin':'50% 0',
                opacity:0
            }),
            animate('800ms ease', style({transform:'scale(1)', opacity:1}))
        ])
    ]),
    trigger('fadeElement',[
      state('updated',style({opacity:0})),
      transition("*=>updated",
        animate('2s 1s ease-out'))
    ]),
    trigger('add-new-user-field', [
        state('untriggered', style({
            transform:'translateX(120%)'
        })),
        state('triggered', style({
            transform:'translateX(0)'
        })),
        transition('untriggered => triggered', animate('400ms ease')),
        transition('triggered => untriggered', animate('400ms 100ms ease'))
    ]),
    trigger('scroll-on-top-btn',[
        transition(":enter", [style({opacity:0, transform:'scale(0)'}), animate('400ms ease', style({'opacity':1, transform:'scale(1)'}))]),
        transition(":leave", [style({opacity:1,transform:'scale(1)'}), animate('400ms ease', style({'opacity':0, transform:'scale(0)'}))])
    ]),
    trigger('action-btn',[
        transition(":enter", [style({opacity:0, transform:'scale(0)'}), animate('400ms ease', style({'opacity':1, transform:'scale(1)'}))]),
        transition(":leave", [style({opacity:1,transform:'scale(1)'}), animate('400ms ease', style({'opacity':0, transform:'scale(0)'}))])
    ]),
    trigger('fade-in',[
        transition(":enter", [style({opacity:0}), animate('1000ms 800ms ease', style({'opacity':1}))]),
    ]),
    trigger('fade-in-fast',[
        transition(":enter", [style({opacity:0}), animate('800ms 100ms ease', style({'opacity':1}))]),
    ]),

    
]